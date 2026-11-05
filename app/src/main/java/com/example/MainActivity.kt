package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.data.ai.AiTutorService
import com.example.data.curriculum.AppLocalization
import com.example.data.local.OfflineDatabase
import com.example.data.model.UserProfile
import com.example.data.repository.EnglishMasterRepository
import com.example.ui.navigation.EnglishMasterBottomNavigationBar
import com.example.ui.navigation.EnglishMasterNavHost
import com.example.ui.navigation.HomePlaceholderScreen
import com.example.ui.navigation.ROUTE_LEARN
import com.example.ui.navigation.ROUTE_PRACTICE
import com.example.ui.screens.AiTutorTabScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CelebrationModalOverlay
import com.example.ui.screens.CommunityChatScreen
import com.example.ui.screens.DashboardTabScreen
import com.example.ui.screens.LearnModulesTabScreen
import com.example.ui.screens.LearningPathsScreen
import com.example.ui.screens.PlacementTestScreen
import com.example.ui.screens.PracticeAndGamificationTabScreen
import com.example.ui.screens.ProfileAdminOfflineTabScreen
import com.example.ui.screens.RankingAndAchievementsTabScreen
import com.example.ui.screens.performSignOut
import com.example.ui.theme.EnglishMasterTheme
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.EnglishMasterViewModel
import com.example.util.SpeechController
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent BEFORE UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            EnglishMasterRootApp()
        }
    }
}

@Composable
fun EnglishMasterRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val systemDark = isSystemInDarkTheme()

    val user = currentUser
    val activeUserId = user?.uid ?: "guest_local"
    val activeDisplayName = user?.displayName ?: "Invité 👤"
    val activeEmail = user?.email

    AuthenticatedAppShell(
        currentUserId = activeUserId,
        displayName = activeDisplayName,
        email = activeEmail,
        systemDark = systemDark
    )
}

@Composable
private fun AuthenticatedAppShell(
    currentUserId: String,
    displayName: String,
    email: String?,
    systemDark: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val speechController = remember { SpeechController(context) }
    DisposableEffect(Unit) {
        onDispose { speechController.shutdown() }
    }

    val viewModel: EnglishMasterViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val offlineDb = OfflineDatabase.getInstance(app)
                EnglishMasterViewModel(
                    repository = EnglishMasterRepository(db),
                    offlineDao = offlineDb.offlineDao(),
                    aiTutorService = AiTutorService(),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    initialEmail = email
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val uiLanguage by viewModel.uiLanguage.collectAsStateWithLifecycle()
    val darkOverride by viewModel.darkModeOverride.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcardsState.collectAsStateWithLifecycle()
    val progressList by viewModel.progressState.collectAsStateWithLifecycle()
    val tutorMessages by viewModel.tutorMessagesState.collectAsStateWithLifecycle()
    val latestTutorReply by viewModel.latestTutorReply.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val customCatalog by viewModel.catalogState.collectAsStateWithLifecycle()
    val downloadedPacks by viewModel.downloadedPacks.collectAsStateWithLifecycle()
    val pendingOfflineScores by viewModel.pendingOfflineScores.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val writingResult by viewModel.writingResult.collectAsStateWithLifecycle()
    val generatedQuiz by viewModel.generatedQuiz.collectAsStateWithLifecycle()
    val selectedCommunityRoomId by viewModel.selectedCommunityRoomId.collectAsStateWithLifecycle()
    val selectedTandemPeer by viewModel.selectedTandemPeer.collectAsStateWithLifecycle()
    val communityMessages by viewModel.communityMessagesState.collectAsStateWithLifecycle()
    val aiDraftSuggestion by viewModel.aiDraftSuggestion.collectAsStateWithLifecycle()
    val heartsCount by viewModel.heartsCount.collectAsStateWithLifecycle()
    val completedUnitIds by viewModel.completedUnitIdsState.collectAsStateWithLifecycle()
    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()

    val isGuestMode = viewModel.isGuestMode
    val isDark = darkOverride ?: systemDark
    val layoutDirection = if (uiLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = AppLocalization.forLang(uiLanguage)

    val navController = rememberNavController()
    var selectedLearnModuleKey by remember { mutableStateOf("vocabulary") }
    var selectedPracticeSubMode by remember { mutableStateOf("quiz_games") } // "quiz_games", "ai_tutor", "community"
    var showPlacementOverlay by remember { mutableStateOf(false) }
    var showPathsOverlay by remember { mutableStateOf(false) }
    var showAuthOverlay by remember { mutableStateOf(false) }

    val activeProfile = (profileState as? DataUiState.Success)?.data ?: UserProfile(
        id = currentUserId,
        userId = currentUserId,
        displayName = displayName,
        email = email,
        uiLanguage = uiLanguage
    )

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        EnglishMasterTheme(darkTheme = isDark) {
            if (celebrationEvent != null) {
                CelebrationModalOverlay(
                    event = celebrationEvent!!,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }

            if (showAuthOverlay) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthScreen(
                        selectedLang = uiLanguage,
                        onChangeLang = { viewModel.setLanguage(it) },
                        onAuthSuccess = { showAuthOverlay = false },
                        onContinueAsGuest = { showAuthOverlay = false }
                    )
                }
            } else {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (!showPlacementOverlay && !showPathsOverlay) {
                            EnglishMasterBottomNavigationBar(
                                navController = navController,
                                strings = strings
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedVisibility(visible = statusBanner != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = statusBanner.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.dismissBanner() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                                    }
                                }
                            }
                        }

                        if (profileState is DataUiState.Loading && activeProfile.xp == 0L) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                HomePlaceholderScreen()
                                CircularProgressIndicator()
                            }
                        } else if (showPlacementOverlay) {
                            PlacementTestScreen(
                                onSpeakText = { text ->
                                    speechController.setAccent(true)
                                    speechController.setSpeed(1.0f)
                                    speechController.speak(text)
                                },
                                onFinishPlacement = { correct, total, cefr, trackId ->
                                    viewModel.completePlacementTest(correct, total, cefr, trackId)
                                },
                                onClose = { showPlacementOverlay = false }
                            )
                        } else if (showPathsOverlay) {
                            LearningPathsScreen(
                                profile = activeProfile,
                                onSelectTrack = { track ->
                                    viewModel.updateProfilePreferences(
                                        displayName = activeProfile.displayName,
                                        cefrLevel = activeProfile.cefrLevel,
                                        dailyGoalMinutes = activeProfile.dailyGoalMinutes,
                                        selectedTrackId = track.id
                                    )
                                },
                                onCompleteTrackUnit = { track, unitName ->
                                    viewModel.recordModuleActivity(
                                        moduleType = "path",
                                        itemId = "${track.id}_$unitName",
                                        title = "${track.titleEn}: $unitName",
                                        cefrLevel = activeProfile.cefrLevel,
                                        score = 100,
                                        xpEarned = 30,
                                        feedback = "Unit completed"
                                    )
                                },
                                onClose = { showPathsOverlay = false }
                            )
                        } else {
                            EnglishMasterNavHost(
                                navController = navController,
                                modifier = Modifier.fillMaxSize(),
                                homeContent = {
                                    DashboardTabScreen(
                                        profile = activeProfile,
                                        strings = strings,
                                        recentProgress = progressList,
                                        isGuestMode = isGuestMode,
                                        heartsCount = heartsCount,
                                        completedUnitIds = completedUnitIds,
                                        onRefillHearts = { viewModel.refillHearts() },
                                        onConsumeHeart = { viewModel.consumeHeartOnMistake() },
                                        onCompleteGamifiedUnit = { unitId, title, cefr, xp, stars ->
                                            viewModel.completeGamifiedUnit(unitId, title, cefr, xp, stars)
                                        },
                                        onSpeakText = { text ->
                                            speechController.setAccent(true)
                                            speechController.setSpeed(1.0f)
                                            speechController.speak(text)
                                        },
                                        onOpenSaveProgressAuth = { showAuthOverlay = true },
                                        onOpenPlacementTest = { showPlacementOverlay = true },
                                        onOpenLearningPaths = { showPathsOverlay = true },
                                        onNavigateToModule = { moduleKey ->
                                            selectedLearnModuleKey = moduleKey
                                            navigateToRoute(ROUTE_LEARN)
                                        },
                                        onOpenTutor = {
                                            selectedPracticeSubMode = "ai_tutor"
                                            navigateToRoute(ROUTE_PRACTICE)
                                        },
                                        onOpenCommunityChat = {
                                            selectedPracticeSubMode = "community"
                                            navigateToRoute(ROUTE_PRACTICE)
                                        }
                                    )
                                },
                                learnContent = {
                                    LearnModulesTabScreen(
                                        selectedModuleKey = selectedLearnModuleKey,
                                        onSelectModuleKey = { selectedLearnModuleKey = it },
                                        flashcards = flashcards,
                                        customCatalog = customCatalog,
                                        isAiLoading = isAiLoading,
                                        writingResult = writingResult,
                                        onSpeakText = { text, isUk, speed ->
                                            speechController.setAccent(isUk)
                                            speechController.setSpeed(speed)
                                            speechController.speak(text)
                                        },
                                        onAddFlashcard = { word, fr, ar, phonetic, ex, cat, cefr ->
                                            viewModel.addVocabToFlashcards(word, fr, ar, phonetic, ex, cat, cefr)
                                        },
                                        onReviewFlashcard = { card, remembered ->
                                            viewModel.reviewFlashcard(card, remembered)
                                        },
                                        onRecordModuleScore = { mod, id, title, cefr, score, xp, fb ->
                                            viewModel.recordModuleActivity(mod, id, title, cefr, score, xp, fb)
                                        },
                                        onEvaluateWriting = { text, promptTitle, cefr ->
                                            viewModel.evaluateUserWriting(text, promptTitle, cefr)
                                        }
                                    )
                                },
                                practiceContent = {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FilterChip(
                                                selected = selectedPracticeSubMode == "quiz_games",
                                                onClick = { selectedPracticeSubMode = "quiz_games" },
                                                label = { Text("🎮 Games & Quiz") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("switch_to_quiz_games")
                                            )
                                            FilterChip(
                                                selected = selectedPracticeSubMode == "ai_tutor",
                                                onClick = { selectedPracticeSubMode = "ai_tutor" },
                                                label = { Text("🤖 AI Tutor") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("switch_to_ai_tutor")
                                            )
                                            FilterChip(
                                                selected = selectedPracticeSubMode == "community",
                                                onClick = { selectedPracticeSubMode = "community" },
                                                label = { Text("💬 Salon") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("switch_to_community_chat")
                                            )
                                        }

                                        when (selectedPracticeSubMode) {
                                            "quiz_games" -> PracticeAndGamificationTabScreen(
                                                profile = activeProfile,
                                                leaderboard = leaderboard,
                                                generatedQuiz = generatedQuiz,
                                                isAiLoading = isAiLoading,
                                                heartsCount = heartsCount,
                                                onMistakeConsumeHeart = { viewModel.consumeHeartOnMistake() },
                                                onSpeakText = { text ->
                                                    speechController.setAccent(true)
                                                    speechController.setSpeed(1.0f)
                                                    speechController.speak(text)
                                                },
                                                onRecordExerciseResult = { id, title, cefr, score, xp, fb ->
                                                    viewModel.recordModuleActivity("quiz", id, title, cefr, score, xp, fb)
                                                },
                                                onGenerateAiExercise = { topic, cefr ->
                                                    viewModel.generateAiExercise(topic, cefr)
                                                },
                                                onStartTandemWithUser = { peerUid, peerName ->
                                                    viewModel.startTandemChatWithPeer(peerUid, peerName)
                                                    selectedPracticeSubMode = "community"
                                                }
                                            )
                                            "ai_tutor" -> AiTutorTabScreen(
                                                profile = activeProfile,
                                                messages = tutorMessages,
                                                latestReply = latestTutorReply,
                                                isAiLoading = isAiLoading,
                                                onSendMessage = { text, scenario ->
                                                    viewModel.sendTutorMessage(text, scenario)
                                                },
                                                onSpeakText = { text ->
                                                    speechController.setAccent(true)
                                                    speechController.setSpeed(1.0f)
                                                    speechController.speak(text)
                                                }
                                            )
                                            else -> CommunityChatScreen(
                                                profile = activeProfile,
                                                selectedRoomId = selectedCommunityRoomId,
                                                selectedTandemPeer = selectedTandemPeer,
                                                messages = communityMessages,
                                                activeLearners = leaderboard,
                                                aiDraftSuggestion = aiDraftSuggestion,
                                                isAiLoading = isAiLoading,
                                                onSelectChannel = { roomId ->
                                                    viewModel.selectCommunityChannel(roomId)
                                                },
                                                onStartTandemWithPeer = { peerUid, peerName ->
                                                    viewModel.startTandemChatWithPeer(peerUid, peerName)
                                                },
                                                onSendMessage = { content ->
                                                    viewModel.sendCommunityChatMessage(content)
                                                },
                                                onSubmitPeerCorrection = { msgId, correction ->
                                                    viewModel.submitPeerGrammarCorrection(msgId, correction)
                                                },
                                                onLikeMessage = { msgId, likes ->
                                                    viewModel.likeCommunityMessage(msgId, likes)
                                                },
                                                onDeleteMessage = { msgId ->
                                                    viewModel.deleteCommunityMessage(msgId)
                                                },
                                                onCheckDraftWithAi = { draft ->
                                                    viewModel.checkMessageDraftWithAi(draft)
                                                },
                                                onDismissDraftSuggestion = {
                                                    viewModel.clearAiDraftSuggestion()
                                                },
                                                onSpeakText = { text ->
                                                    speechController.setAccent(true)
                                                    speechController.setSpeed(1.0f)
                                                    speechController.speak(text)
                                                }
                                            )
                                        }
                                    }
                                },
                                rankingContent = {
                                    RankingAndAchievementsTabScreen(
                                        profile = activeProfile,
                                        leaderboard = leaderboard,
                                        isGuestMode = isGuestMode,
                                        onOpenSaveProgressAuth = { showAuthOverlay = true },
                                        onStartTandemWithUser = { peerUid, peerName ->
                                            viewModel.startTandemChatWithPeer(peerUid, peerName)
                                            selectedPracticeSubMode = "community"
                                            navigateToRoute(ROUTE_PRACTICE)
                                        }
                                    )
                                },
                                profileContent = {
                                    ProfileAdminOfflineTabScreen(
                                        profile = activeProfile,
                                        isGuestMode = isGuestMode,
                                        isDarkTheme = isDark,
                                        downloadedPacks = downloadedPacks,
                                        pendingOfflineScores = pendingOfflineScores,
                                        customCatalog = customCatalog,
                                        onOpenSaveProgressAuth = { showAuthOverlay = true },
                                        onToggleDarkTheme = { viewModel.toggleDarkMode(isDark) },
                                        onChangeLanguage = { lang -> viewModel.setLanguage(lang) },
                                        onSaveProfile = { name, cefr, goal, trackId ->
                                            viewModel.updateProfilePreferences(name, cefr, goal, trackId)
                                        },
                                        onDownloadOfflinePack = { packId, title, cefr, modType ->
                                            viewModel.downloadOfflinePack(packId, title, cefr, modType)
                                        },
                                        onRemoveOfflinePack = { packId ->
                                            viewModel.removeOfflinePack(packId)
                                        },
                                        onSyncOfflineQueue = {
                                            viewModel.syncOfflineQueueToCloud()
                                        },
                                        onPublishAdminItem = { type, title, sub, cefr, body, fr, ar, q, opts, ans ->
                                            viewModel.publishAdminCatalogItem(type, title, sub, cefr, body, fr, ar, q, opts, ans)
                                        },
                                        onImportCsvBatch = { csv ->
                                            viewModel.importCsvCatalogBatch(csv)
                                        },
                                        onDeleteCatalogItem = { id ->
                                            viewModel.deleteAdminCatalogItem(id)
                                        },
                                        onSignOut = {
                                            performSignOut(context, scope) {}
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
