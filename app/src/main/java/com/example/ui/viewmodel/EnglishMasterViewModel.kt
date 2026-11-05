package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiTutorService
import com.example.data.ai.GeneratedQuizItem
import com.example.data.ai.TutorStructuredReply
import com.example.data.ai.WritingAssessmentResult
import com.example.data.curriculum.EnglishCurriculum
import com.example.data.local.LocalGuestFlashcardEntity
import com.example.data.local.LocalGuestProfileEntity
import com.example.data.local.OfflineDao
import com.example.data.local.OfflinePackEntity
import com.example.data.local.OfflinePendingScoreEntity
import com.example.data.model.CatalogItem
import com.example.data.model.CommunityMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LearningProgress
import com.example.data.model.TutorMessage
import com.example.data.model.UserFlashcard
import com.example.data.model.UserProfile
import com.example.data.repository.EnglishMasterRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "EnglishMasterVM"

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

data class CelebrationEvent(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val xpBonus: Int
)

class EnglishMasterViewModel(
    private val repository: EnglishMasterRepository,
    private val offlineDao: OfflineDao,
    private val aiTutorService: AiTutorService,
    val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String?
) : ViewModel() {

    val isGuestMode: Boolean = currentUserId == "guest_local"

    private val _uiLanguage = MutableStateFlow("fr")
    val uiLanguage: StateFlow<String> = _uiLanguage.asStateFlow()

    private val _darkModeOverride = MutableStateFlow<Boolean?>(null)
    val darkModeOverride: StateFlow<Boolean?> = _darkModeOverride.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _celebrationEvent = MutableStateFlow<CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationEvent?> = _celebrationEvent.asStateFlow()

    private val _heartsCount = MutableStateFlow(5)
    val heartsCount: StateFlow<Int> = _heartsCount.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _latestTutorReply = MutableStateFlow<TutorStructuredReply?>(null)
    val latestTutorReply: StateFlow<TutorStructuredReply?> = _latestTutorReply.asStateFlow()

    private val _writingResult = MutableStateFlow<WritingAssessmentResult?>(null)
    val writingResult: StateFlow<WritingAssessmentResult?> = _writingResult.asStateFlow()

    private val _generatedQuiz = MutableStateFlow<GeneratedQuizItem?>(null)
    val generatedQuiz: StateFlow<GeneratedQuizItem?> = _generatedQuiz.asStateFlow()

    private val _selectedCommunityRoomId = MutableStateFlow("general")
    val selectedCommunityRoomId: StateFlow<String> = _selectedCommunityRoomId.asStateFlow()

    private val _selectedTandemPeer = MutableStateFlow<Pair<String, String>?>(null)
    val selectedTandemPeer: StateFlow<Pair<String, String>?> = _selectedTandemPeer.asStateFlow()

    private val _aiDraftSuggestion = MutableStateFlow<String?>(null)
    val aiDraftSuggestion: StateFlow<String?> = _aiDraftSuggestion.asStateFlow()

    private val _guestTutorMessages = MutableStateFlow<List<TutorMessage>>(emptyList())

    // Unified Profile State (Local Room in Guest Mode 👤 OR Cloud Firestore in Connected Mode 🔐)
    private val profileFlow: Flow<DataUiState<UserProfile>> = if (isGuestMode) {
        offlineDao.observeGuestProfile().map { guest ->
            if (guest != null) {
                _uiLanguage.value = guest.uiLanguage
                _heartsCount.value = guest.hearts
                DataUiState.Success(
                    UserProfile(
                        id = "guest_local",
                        userId = "guest_local",
                        displayName = guest.displayName,
                        email = null,
                        avatarTag = "barnaby",
                        cefrLevel = guest.cefrLevel,
                        uiLanguage = guest.uiLanguage,
                        xp = guest.xp,
                        streakDays = guest.streakDays,
                        dailyGoalMinutes = guest.dailyGoalMinutes,
                        dailyMinutesCompleted = guest.dailyMinutesCompleted,
                        wordsLearned = guest.wordsLearned,
                        lessonsCompleted = guest.lessonsCompleted,
                        grammarScore = guest.grammarScore,
                        pronunciationScore = guest.pronunciationScore,
                        listeningScore = guest.listeningScore,
                        writingScore = guest.writingScore,
                        placementCompleted = guest.placementCompleted,
                        subscriptionTier = "Free",
                        selectedTrackId = guest.selectedTrackId
                    )
                )
            } else {
                DataUiState.Loading
            }
        }
    } else {
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, DataUiState<UserProfile>> { profile ->
                if (profile != null) {
                    _uiLanguage.value = profile.uiLanguage
                    DataUiState.Success(profile)
                } else {
                    DataUiState.Loading
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing profile", rootError)
                emit(DataUiState.Error(rootError.message ?: "Failed to load profile"))
            }
    }

    val profileState: StateFlow<DataUiState<UserProfile>> = profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DataUiState.Loading
    )

    // Unified Flashcards State
    val flashcardsState: StateFlow<List<UserFlashcard>> = (if (isGuestMode) {
        offlineDao.observeGuestFlashcards().map { list ->
            list.map { g ->
                UserFlashcard(
                    id = g.cardId,
                    userId = "guest_local",
                    word = g.word,
                    translationFr = g.translationFr,
                    translationAr = g.translationAr,
                    phonetic = g.phonetic,
                    example = g.example,
                    category = g.category,
                    cefrLevel = g.cefrLevel,
                    isFavorite = g.isFavorite,
                    boxLevel = g.boxLevel,
                    nextReviewEpochDay = g.nextReviewEpochDay,
                    mastered = g.mastered
                )
            }
        }
    } else {
        repository.observeFlashcards(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing flashcards", firestoreError ?: error)
                emit(emptyList())
            }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Unified Progress History State
    val progressState: StateFlow<List<LearningProgress>> = (if (isGuestMode) {
        offlineDao.observePendingScores().map { scores ->
            scores.map { s ->
                LearningProgress(
                    id = "local_${s.localId}",
                    userId = "guest_local",
                    moduleType = s.moduleType,
                    itemId = s.itemId,
                    title = s.title,
                    cefrLevel = s.cefrLevel,
                    score = s.score.toLong(),
                    maxScore = s.maxScore.toLong(),
                    xpEarned = s.xpEarned.toLong(),
                    feedback = s.feedback
                )
            }
        }
    } else {
        repository.observeProgress(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing progress", firestoreError ?: error)
                emit(emptyList())
            }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Completed Gamified Unit IDs (derived from local guest profile + progress history)
    val completedUnitIdsState: StateFlow<Set<String>> = combine(
        offlineDao.observeGuestProfile(),
        progressState
    ) { guestProfile, progressList ->
        val fromGuestCsv = guestProfile?.completedUnitIdsCsv
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
        val fromProgress = progressList.map { it.itemId }
            .filter { it.startsWith("unit_") }
            .toSet()
        fromGuestCsv + fromProgress
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val tutorMessagesState: StateFlow<List<TutorMessage>> = (if (isGuestMode) {
        _guestTutorMessages
    } else {
        repository.observeTutorMessages(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing tutor messages", firestoreError ?: error)
                emit(emptyList())
            }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val leaderboardState: StateFlow<List<LeaderboardEntry>> = (if (isGuestMode) {
        flowOf(emptyList())
    } else {
        repository.observeLeaderboard()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing leaderboard", firestoreError ?: error)
                emit(emptyList())
            }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val catalogState: StateFlow<List<CatalogItem>> = (if (isGuestMode) {
        flowOf(emptyList())
    } else {
        repository.observePublishedCatalog()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing catalog", firestoreError ?: error)
                emit(emptyList())
            }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val communityMessagesState: StateFlow<List<CommunityMessage>> = (if (isGuestMode) {
        flowOf(emptyList())
    } else {
        _selectedCommunityRoomId.flatMapLatest { roomId ->
            repository.observeCommunityMessages(roomId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing community messages for room $roomId", firestoreError ?: error)
                    emit(emptyList())
                }
        }
    }).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val downloadedPacks: StateFlow<List<OfflinePackEntity>> = offlineDao.observeDownloadedPacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val pendingOfflineScores: StateFlow<List<OfflinePendingScoreEntity>> = offlineDao.observePendingScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        if (isGuestMode) {
            ensureGuestProfileInitialized()
        } else {
            ensureAuthenticatedProfileAndMigrateGuestData()
        }
    }

    private fun ensureGuestProfileInitialized() {
        viewModelScope.launch {
            val existingGuest = offlineDao.getGuestProfileOnce()
            if (existingGuest == null) {
                offlineDao.saveGuestProfile(
                    LocalGuestProfileEntity(
                        id = "guest_local",
                        displayName = "Explorateur Invité 👤",
                        cefrLevel = "A1",
                        uiLanguage = "fr",
                        xp = 50L,
                        streakDays = 1L,
                        hearts = 5,
                        dailyGoalMinutes = 15L,
                        dailyMinutesCompleted = 5L,
                        wordsLearned = 3L,
                        lessonsCompleted = 0L
                    )
                )
                EnglishCurriculum.vocabularyCatalog.take(3).forEachIndexed { idx, vocab ->
                    offlineDao.saveGuestFlashcard(
                        LocalGuestFlashcardEntity(
                            cardId = "guest_card_$idx",
                            word = vocab.word,
                            translationFr = vocab.translationFr,
                            translationAr = vocab.translationAr,
                            phonetic = vocab.phonetic,
                            example = vocab.exampleEn,
                            category = vocab.category,
                            cefrLevel = vocab.cefrLevel,
                            isFavorite = true,
                            boxLevel = 1L,
                            nextReviewEpochDay = System.currentTimeMillis() / 86_400_000L,
                            mastered = false,
                            migratedToCloud = false
                        )
                    )
                }
            } else {
                _heartsCount.value = existingGuest.hearts
            }
        }
    }

    /**
     * Section 4 & 8: Intelligent Guest-to-Cloud Account Conversion & Merge without data loss.
     * Never deletes local guest data prior to cloud confirmation.
     */
    private fun ensureAuthenticatedProfileAndMigrateGuestData() {
        viewModelScope.launch {
            val existingCloud = repository.getUserProfileById(currentUserId).getOrNull()
            val localGuest = offlineDao.getGuestProfileOnce()
            val unmigratedCards = offlineDao.getGuestFlashcardsOnce().filter { !it.migratedToCloud }
            val unmigratedScores = offlineDao.getPendingScoresList()

            val hasUnmigratedGuestProgress = localGuest != null && (
                localGuest.xp > 50L + localGuest.xpAlreadyMigrated ||
                    localGuest.lessonsCompleted > localGuest.lessonsAlreadyMigrated ||
                    unmigratedCards.isNotEmpty() ||
                    unmigratedScores.isNotEmpty()
                )

            if (existingCloud == null) {
                val startCefr = localGuest?.cefrLevel ?: "A1"
                val startLang = localGuest?.uiLanguage ?: "fr"
                val startGoal = localGuest?.dailyGoalMinutes ?: 15L
                val startXp = localGuest?.xp?.coerceAtLeast(50L) ?: 50L
                val startStreak = localGuest?.streakDays?.coerceAtLeast(1L) ?: 1L
                val startWords = localGuest?.wordsLearned?.coerceAtLeast(3L) ?: 3L
                val startLessons = localGuest?.lessonsCompleted ?: 0L

                val createRes = repository.createInitialProfile(
                    displayName = initialDisplayName,
                    email = initialEmail,
                    cefrLevel = startCefr,
                    uiLanguage = startLang,
                    dailyGoalMinutes = startGoal
                )
                if (createRes.isSuccess && localGuest != null) {
                    repository.updateProfileFields(
                        mapOf(
                            "xp" to startXp,
                            "streakDays" to startStreak,
                            "dailyMinutesCompleted" to localGuest.dailyMinutesCompleted,
                            "wordsLearned" to startWords,
                            "lessonsCompleted" to startLessons,
                            "grammarScore" to localGuest.grammarScore,
                            "pronunciationScore" to localGuest.pronunciationScore,
                            "listeningScore" to localGuest.listeningScore,
                            "writingScore" to localGuest.writingScore,
                            "placementCompleted" to localGuest.placementCompleted,
                            "selectedTrackId" to localGuest.selectedTrackId
                        )
                    )
                }

                if (unmigratedCards.isEmpty()) {
                    EnglishCurriculum.vocabularyCatalog.take(3).forEach { vocab ->
                        repository.createFlashcard(
                            word = vocab.word,
                            translationFr = vocab.translationFr,
                            translationAr = vocab.translationAr,
                            phonetic = vocab.phonetic,
                            example = vocab.exampleEn,
                            category = vocab.category,
                            cefrLevel = vocab.cefrLevel
                        )
                    }
                }

                repository.syncLeaderboardEntry(
                    displayName = initialDisplayName,
                    cefrLevel = startCefr,
                    xp = startXp,
                    streakDays = startStreak,
                    badgesCount = EnglishCurriculum.badges.count { startXp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                )
                repository.sendCommunityMessage(
                    senderName = initialDisplayName,
                    senderCefr = startCefr,
                    roomId = "general",
                    content = "Hello everyone! I just joined English Master AI to practice my English with the community!"
                )
            } else if (hasUnmigratedGuestProgress && localGuest != null) {
                // Intelligent merge with existing cloud profile
                val deltaXp = (localGuest.xp - 50L - localGuest.xpAlreadyMigrated).coerceAtLeast(0L)
                val deltaLessons = (localGuest.lessonsCompleted - localGuest.lessonsAlreadyMigrated).coerceAtLeast(0L)
                val mergedXp = maxOf(existingCloud.xp, localGuest.xp) + deltaXp
                val mergedLessons = maxOf(existingCloud.lessonsCompleted, localGuest.lessonsCompleted) + deltaLessons
                val mergedStreak = maxOf(existingCloud.streakDays, localGuest.streakDays)
                val mergedWords = maxOf(existingCloud.wordsLearned, localGuest.wordsLearned)
                val mergedCefr = if (cefrRank(localGuest.cefrLevel) > cefrRank(existingCloud.cefrLevel)) {
                    localGuest.cefrLevel
                } else {
                    existingCloud.cefrLevel
                }

                repository.updateProfileFields(
                    mapOf(
                        "xp" to mergedXp,
                        "lessonsCompleted" to mergedLessons,
                        "streakDays" to mergedStreak,
                        "wordsLearned" to mergedWords,
                        "cefrLevel" to mergedCefr,
                        "grammarScore" to maxOf(existingCloud.grammarScore, localGuest.grammarScore),
                        "pronunciationScore" to maxOf(existingCloud.pronunciationScore, localGuest.pronunciationScore),
                        "listeningScore" to maxOf(existingCloud.listeningScore, localGuest.listeningScore),
                        "writingScore" to maxOf(existingCloud.writingScore, localGuest.writingScore),
                        "placementCompleted" to (existingCloud.placementCompleted || localGuest.placementCompleted)
                    )
                )
                repository.syncLeaderboardEntry(
                    displayName = existingCloud.displayName,
                    cefrLevel = mergedCefr,
                    xp = mergedXp,
                    streakDays = mergedStreak,
                    badgesCount = EnglishCurriculum.badges.count { mergedXp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                )
            }

            // Migrate any unmigrated local guest flashcards without deleting local backup prematurely
            for (card in unmigratedCards) {
                val res = repository.createFlashcard(
                    word = card.word,
                    translationFr = card.translationFr,
                    translationAr = card.translationAr,
                    phonetic = card.phonetic,
                    example = card.example,
                    category = card.category,
                    cefrLevel = card.cefrLevel,
                    isFavorite = card.isFavorite,
                    boxLevel = card.boxLevel,
                    nextReviewEpochDay = card.nextReviewEpochDay
                )
                if (res.isSuccess) {
                    offlineDao.markGuestFlashcardMigrated(card.cardId)
                }
            }

            // Migrate any unmigrated local guest progress records
            var migratedRecordsCount = 0
            for (scoreItem in unmigratedScores) {
                val res = repository.recordProgress(
                    moduleType = scoreItem.moduleType,
                    itemId = scoreItem.itemId,
                    title = scoreItem.title,
                    cefrLevel = scoreItem.cefrLevel,
                    score = scoreItem.score.toLong(),
                    maxScore = scoreItem.maxScore.toLong(),
                    xpEarned = scoreItem.xpEarned.toLong(),
                    feedback = scoreItem.feedback.ifBlank { "Migré depuis le Mode Invité 👤" }
                )
                if (res.isSuccess) {
                    offlineDao.markScoreSynced(scoreItem.localId)
                    migratedRecordsCount++
                }
            }

            if (localGuest != null && hasUnmigratedGuestProgress) {
                offlineDao.saveGuestProfile(
                    localGuest.copy(
                        migratedToUid = currentUserId,
                        xpAlreadyMigrated = (localGuest.xp - 50L).coerceAtLeast(0L),
                        lessonsAlreadyMigrated = localGuest.lessonsCompleted,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                _celebrationEvent.value = CelebrationEvent(
                    emoji = "☁️",
                    title = "Progression Sauvegardée !",
                    subtitle = "Toutes vos données du Mode Invité (${localGuest.xp} XP, $migratedRecordsCount leçons) sont synchronisées sur votre compte !",
                    xpBonus = 25
                )
            }
        }
    }

    private fun cefrRank(cefr: String): Int = when (cefr) {
        "A1" -> 1
        "A2" -> 2
        "B1" -> 3
        "B2" -> 4
        "C1" -> 5
        "C2" -> 6
        else -> 1
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    fun refillHearts() {
        _heartsCount.value = 5
        viewModelScope.launch {
            val guest = offlineDao.getGuestProfileOnce()
            if (guest != null) {
                offlineDao.saveGuestProfile(guest.copy(hearts = 5))
            }
            _statusBanner.value = "❤️ Vies rechargées à 5/5 ! Continuez votre apprentissage !"
        }
    }

    fun consumeHeartOnMistake() {
        val nextHearts = (_heartsCount.value - 1).coerceAtLeast(1)
        _heartsCount.value = nextHearts
        viewModelScope.launch {
            val guest = offlineDao.getGuestProfileOnce()
            if (guest != null) {
                offlineDao.saveGuestProfile(guest.copy(hearts = nextHearts))
            }
        }
    }

    fun setLanguage(lang: String) {
        _uiLanguage.value = lang
        viewModelScope.launch {
            if (isGuestMode) {
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                offlineDao.saveGuestProfile(guest.copy(uiLanguage = lang))
            } else {
                repository.updateProfileFields(mapOf("uiLanguage" to lang))
            }
        }
    }

    fun toggleDarkMode(currentDark: Boolean) {
        _darkModeOverride.value = !currentDark
    }

    fun updateProfilePreferences(
        displayName: String,
        cefrLevel: String,
        dailyGoalMinutes: Long,
        selectedTrackId: String
    ) {
        viewModelScope.launch {
            val cleanName = displayName.take(80).ifBlank { if (isGuestMode) "Invité 👤" else "Learner" }
            if (isGuestMode) {
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                offlineDao.saveGuestProfile(
                    guest.copy(
                        displayName = cleanName,
                        cefrLevel = cefrLevel,
                        dailyGoalMinutes = dailyGoalMinutes.coerceIn(5L, 240L),
                        selectedTrackId = selectedTrackId,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                _statusBanner.value = "💾 Profil invité enregistré sur cet appareil !"
            } else {
                val result = repository.updateProfileFields(
                    mapOf(
                        "displayName" to cleanName,
                        "cefrLevel" to cefrLevel,
                        "dailyGoalMinutes" to dailyGoalMinutes.coerceIn(5L, 240L),
                        "selectedTrackId" to selectedTrackId
                    )
                )
                if (result.isSuccess) {
                    val current = (profileState.value as? DataUiState.Success)?.data
                    if (current != null) {
                        repository.syncLeaderboardEntry(
                            displayName = cleanName,
                            cefrLevel = cefrLevel,
                            xp = current.xp,
                            streakDays = current.streakDays,
                            badgesCount = EnglishCurriculum.badges.count { current.xp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                        )
                    }
                    _statusBanner.value = "☁️ Profil synchronisé dans le cloud !"
                }
            }
        }
    }

    fun completePlacementTest(correctCount: Int, totalCount: Int, recommendedLevel: String, recommendedTrackId: String) {
        viewModelScope.launch {
            val percentage = ((correctCount.toFloat() / totalCount.coerceAtLeast(1)) * 100).toLong()
            val current = (profileState.value as? DataUiState.Success)?.data
            val newXp = (current?.xp ?: 50L) + 120L

            if (isGuestMode) {
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                offlineDao.saveGuestProfile(
                    guest.copy(
                        cefrLevel = recommendedLevel,
                        placementCompleted = true,
                        selectedTrackId = recommendedTrackId,
                        xp = newXp,
                        lessonsCompleted = guest.lessonsCompleted + 1L,
                        grammarScore = percentage.coerceIn(30L, 100L),
                        listeningScore = percentage.coerceIn(35L, 100L),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                offlineDao.queuePendingScore(
                    OfflinePendingScoreEntity(
                        moduleType = "placement",
                        itemId = "placement_test_v1",
                        title = "Placement Test ($recommendedLevel)",
                        cefrLevel = recommendedLevel,
                        score = percentage.toInt(),
                        maxScore = 100,
                        xpEarned = 120,
                        feedback = "Recommended Level: $recommendedLevel | Score: $correctCount/$totalCount",
                        syncedToCloud = false
                    )
                )
            } else {
                repository.recordProgress(
                    moduleType = "placement",
                    itemId = "placement_test_v1",
                    title = "Placement Test ($recommendedLevel)",
                    cefrLevel = recommendedLevel,
                    score = percentage,
                    maxScore = 100L,
                    xpEarned = 120L,
                    feedback = "Recommended Level: $recommendedLevel | Score: $correctCount/$totalCount"
                )

                repository.updateProfileFields(
                    mapOf(
                        "cefrLevel" to recommendedLevel,
                        "placementCompleted" to true,
                        "selectedTrackId" to recommendedTrackId,
                        "xp" to newXp,
                        "grammarScore" to percentage.coerceIn(30L, 100L),
                        "listeningScore" to percentage.coerceIn(35L, 100L)
                    )
                )

                repository.syncLeaderboardEntry(
                    displayName = current?.displayName ?: initialDisplayName,
                    cefrLevel = recommendedLevel,
                    xp = newXp,
                    streakDays = current?.streakDays ?: 1L,
                    badgesCount = EnglishCurriculum.badges.count { newXp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                )
            }

            _celebrationEvent.value = CelebrationEvent(
                emoji = "🎯",
                title = "Niveau Débloqué : $recommendedLevel !",
                subtitle = "Parcours personnalisé activé avec un score de $correctCount/$totalCount !",
                xpBonus = 120
            )
        }
    }

    fun completeGamifiedUnit(unitId: String, unitTitle: String, cefrLevel: String, xpReward: Int, starsEarned: Int) {
        viewModelScope.launch {
            val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
            val updatedCsv = (guest.completedUnitIdsCsv.split(",").filter { it.isNotBlank() } + unitId)
                .distinct()
                .joinToString(",")
            offlineDao.saveGuestProfile(guest.copy(completedUnitIdsCsv = updatedCsv))

            recordModuleActivity(
                moduleType = "path",
                itemId = unitId,
                title = "$unitTitle (${"⭐".repeat(starsEarned.coerceIn(1, 3))})",
                cefrLevel = cefrLevel,
                score = starsEarned * 33 + 1,
                xpEarned = xpReward,
                feedback = "Unité gamifiée réussie avec $starsEarned étoiles !"
            )
        }
    }

    fun addVocabToFlashcards(
        word: String,
        translationFr: String,
        translationAr: String,
        phonetic: String,
        example: String,
        category: String,
        cefrLevel: String
    ) {
        viewModelScope.launch {
            if (isGuestMode) {
                val cardId = "gcard_${UUID.randomUUID().toString().replace("-", "").take(12)}"
                offlineDao.saveGuestFlashcard(
                    LocalGuestFlashcardEntity(
                        cardId = cardId,
                        word = word,
                        translationFr = translationFr,
                        translationAr = translationAr,
                        phonetic = phonetic,
                        example = example,
                        category = category,
                        cefrLevel = cefrLevel,
                        isFavorite = true,
                        boxLevel = 1L,
                        nextReviewEpochDay = System.currentTimeMillis() / 86_400_000L,
                        mastered = false,
                        migratedToCloud = false
                    )
                )
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                offlineDao.saveGuestProfile(
                    guest.copy(
                        wordsLearned = guest.wordsLearned + 1L,
                        xp = guest.xp + 15L
                    )
                )
                _statusBanner.value = "🧠 '$word' ajouté à vos Flashcards locales (+15 XP ⭐)"
            } else {
                val res = repository.createFlashcard(
                    word = word,
                    translationFr = translationFr,
                    translationAr = translationAr,
                    phonetic = phonetic,
                    example = example,
                    category = category,
                    cefrLevel = cefrLevel
                )
                if (res.isSuccess) {
                    val current = (profileState.value as? DataUiState.Success)?.data
                    if (current != null) {
                        repository.updateProfileFields(
                            mapOf(
                                "wordsLearned" to (current.wordsLearned + 1L),
                                "xp" to (current.xp + 15L)
                            )
                        )
                    }
                    _statusBanner.value = "🧠 '$word' ajouté aux Flashcards SRS (+15 XP ⭐)"
                }
            }
        }
    }

    fun reviewFlashcard(card: UserFlashcard, remembered: Boolean) {
        viewModelScope.launch {
            val xpGain = if (remembered) 20L else 5L
            if (isGuestMode) {
                val newBox = if (remembered) (card.boxLevel + 1L).coerceAtMost(5L) else 1L
                val intervalDays = when (newBox) {
                    1L -> 1L
                    2L -> 2L
                    3L -> 4L
                    4L -> 7L
                    else -> 14L
                }
                val todayEpoch = (System.currentTimeMillis() / 86_400_000L).coerceAtLeast(1L)
                offlineDao.saveGuestFlashcard(
                    LocalGuestFlashcardEntity(
                        cardId = card.id.ifBlank { "gcard_${card.word}" },
                        word = card.word,
                        translationFr = card.translationFr,
                        translationAr = card.translationAr,
                        phonetic = card.phonetic,
                        example = card.example,
                        category = card.category,
                        cefrLevel = card.cefrLevel,
                        isFavorite = card.isFavorite,
                        boxLevel = newBox,
                        nextReviewEpochDay = todayEpoch + intervalDays,
                        mastered = newBox >= 5L,
                        migratedToCloud = false
                    )
                )
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                val newWords = if (remembered && card.boxLevel >= 4L) guest.wordsLearned + 1L else guest.wordsLearned
                offlineDao.saveGuestProfile(
                    guest.copy(
                        xp = guest.xp + xpGain,
                        wordsLearned = newWords,
                        dailyMinutesCompleted = (guest.dailyMinutesCompleted + 2L).coerceAtMost(1440L)
                    )
                )
            } else {
                repository.reviewFlashcard(card, remembered)
                val current = (profileState.value as? DataUiState.Success)?.data
                if (current != null) {
                    val newXp = current.xp + xpGain
                    val newWords = if (remembered && card.boxLevel >= 4L) current.wordsLearned + 1L else current.wordsLearned
                    repository.updateProfileFields(
                        mapOf(
                            "xp" to newXp,
                            "wordsLearned" to newWords,
                            "dailyMinutesCompleted" to (current.dailyMinutesCompleted + 2L).coerceAtMost(1440L)
                        )
                    )
                }
            }
        }
    }

    fun recordModuleActivity(
        moduleType: String,
        itemId: String,
        title: String,
        cefrLevel: String,
        score: Int,
        xpEarned: Int,
        feedback: String
    ) {
        viewModelScope.launch {
            val current = (profileState.value as? DataUiState.Success)?.data
            val prevXp = current?.xp ?: 50L
            val newXp = prevXp + xpEarned
            val prevLessons = current?.lessonsCompleted ?: 0L
            val newLessons = prevLessons + 1L
            val prevMinutes = current?.dailyMinutesCompleted ?: 5L
            val goalMinutes = current?.dailyGoalMinutes ?: 15L
            val newMinutes = (prevMinutes + 5L).coerceAtMost(1440L)

            if (isGuestMode) {
                val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                offlineDao.queuePendingScore(
                    OfflinePendingScoreEntity(
                        moduleType = moduleType,
                        itemId = itemId,
                        title = title,
                        cefrLevel = cefrLevel,
                        score = score,
                        maxScore = 100,
                        xpEarned = xpEarned,
                        feedback = feedback,
                        syncedToCloud = false
                    )
                )
                offlineDao.saveGuestProfile(
                    guest.copy(
                        xp = newXp,
                        lessonsCompleted = newLessons,
                        dailyMinutesCompleted = newMinutes,
                        grammarScore = if (moduleType in listOf("grammar", "quiz")) ((guest.grammarScore + score) / 2L).coerceIn(0L, 100L) else guest.grammarScore,
                        pronunciationScore = if (moduleType == "speaking") ((guest.pronunciationScore + score) / 2L).coerceIn(0L, 100L) else guest.pronunciationScore,
                        listeningScore = if (moduleType == "listening") ((guest.listeningScore + score) / 2L).coerceIn(0L, 100L) else guest.listeningScore,
                        writingScore = if (moduleType == "writing") ((guest.writingScore + score) / 2L).coerceIn(0L, 100L) else guest.writingScore,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            } else {
                val res = repository.recordProgress(
                    moduleType = moduleType,
                    itemId = itemId,
                    title = title,
                    cefrLevel = cefrLevel,
                    score = score.toLong(),
                    maxScore = 100L,
                    xpEarned = xpEarned.toLong(),
                    feedback = feedback
                )
                if (res.isFailure) {
                    offlineDao.queuePendingScore(
                        OfflinePendingScoreEntity(
                            moduleType = moduleType,
                            itemId = itemId,
                            title = title,
                            cefrLevel = cefrLevel,
                            score = score,
                            maxScore = 100,
                            xpEarned = xpEarned,
                            feedback = feedback,
                            syncedToCloud = false
                        )
                    )
                    _statusBanner.value = "💾 Score enregistré hors ligne (synchronisation auto dès connexion)"
                } else if (current != null) {
                    val updatedFields = mutableMapOf<String, Any>(
                        "xp" to newXp,
                        "lessonsCompleted" to newLessons,
                        "dailyMinutesCompleted" to newMinutes
                    )
                    when (moduleType) {
                        "grammar", "quiz" -> updatedFields["grammarScore"] = ((current.grammarScore + score) / 2L).coerceIn(0L, 100L)
                        "speaking" -> updatedFields["pronunciationScore"] = ((current.pronunciationScore + score) / 2L).coerceIn(0L, 100L)
                        "listening" -> updatedFields["listeningScore"] = ((current.listeningScore + score) / 2L).coerceIn(0L, 100L)
                        "writing" -> updatedFields["writingScore"] = ((current.writingScore + score) / 2L).coerceIn(0L, 100L)
                    }
                    repository.updateProfileFields(updatedFields)
                    repository.syncLeaderboardEntry(
                        displayName = current.displayName,
                        cefrLevel = current.cefrLevel,
                        xp = newXp,
                        streakDays = current.streakDays,
                        badgesCount = EnglishCurriculum.badges.count { newXp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                    )
                }
            }

            // Trigger Gamified Celebration Animation
            val newlyUnlockedBadge = EnglishCurriculum.badges.firstOrNull {
                prevXp < it.requiredXp && newXp >= it.requiredXp
            }
            val leveledUp = (newXp / 200L) > (prevXp / 200L)
            val reachedGoalNow = prevMinutes < goalMinutes && newMinutes >= goalMinutes

            _celebrationEvent.value = when {
                newlyUnlockedBadge != null -> CelebrationEvent(
                    emoji = "🏆",
                    title = "Nouveau Badge : ${newlyUnlockedBadge.titleFr} !",
                    subtitle = newlyUnlockedBadge.descriptionFr,
                    xpBonus = xpEarned
                )
                leveledUp -> CelebrationEvent(
                    emoji = "🚀",
                    title = "Niveau ${(newXp / 200L) + 1L} Débloqué !",
                    subtitle = "Bravo ! Vous progressez à grande vitesse avec Barnaby !",
                    xpBonus = xpEarned
                )
                reachedGoalNow -> CelebrationEvent(
                    emoji = "🎯",
                    title = "Objectif Quotidien Atteint !",
                    subtitle = "Vous avez complété vos $goalMinutes minutes d'anglais aujourd'hui ! 🔥",
                    xpBonus = xpEarned
                )
                else -> CelebrationEvent(
                    emoji = "🎉",
                    title = "$title Terminé !",
                    subtitle = if (isGuestMode && newLessons >= 3L) {
                        "🎉 Tu progresses ! Crée un compte pour sauvegarder tes résultats dans le cloud."
                    } else {
                        "Excellent travail ! Score : $score/100"
                    },
                    xpBonus = xpEarned
                )
            }
        }
    }

    fun sendTutorMessage(userText: String, scenario: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val current = (profileState.value as? DataUiState.Success)?.data
            val cefr = current?.cefrLevel ?: "B1"
            val lang = _uiLanguage.value

            if (isGuestMode) {
                _guestTutorMessages.value = _guestTutorMessages.value + TutorMessage(
                    id = "gmsg_${System.currentTimeMillis()}",
                    userId = "guest_local",
                    role = "user",
                    scenario = scenario,
                    content = userText
                )
            } else {
                repository.saveTutorMessage(
                    role = "user",
                    scenario = scenario,
                    content = userText
                )
            }

            val historyPairs = tutorMessagesState.value.takeLast(6).map { it.role to it.content }
            val aiResult = aiTutorService.askTutor(
                userMessage = userText,
                cefrLevel = cefr,
                scenario = scenario,
                uiLanguage = lang,
                recentHistory = historyPairs
            )

            aiResult.onSuccess { reply ->
                _latestTutorReply.value = reply
                if (isGuestMode) {
                    _guestTutorMessages.value = _guestTutorMessages.value + TutorMessage(
                        id = "gmsg_ai_${System.currentTimeMillis()}",
                        userId = "guest_local",
                        role = "assistant",
                        scenario = scenario,
                        content = reply.englishResponse,
                        correction = reply.grammarCorrection,
                        explanation = "${reply.pedagogicalExplanation}\n💡 ${reply.fluencyTip}"
                    )
                    val guest = offlineDao.getGuestProfileOnce() ?: LocalGuestProfileEntity()
                    offlineDao.saveGuestProfile(
                        guest.copy(
                            xp = guest.xp + 25L,
                            dailyMinutesCompleted = (guest.dailyMinutesCompleted + 3L).coerceAtMost(1440L)
                        )
                    )
                } else {
                    repository.saveTutorMessage(
                        role = "assistant",
                        scenario = scenario,
                        content = reply.englishResponse,
                        correction = reply.grammarCorrection,
                        explanation = "${reply.pedagogicalExplanation}\n💡 ${reply.fluencyTip}"
                    )
                    if (current != null) {
                        repository.updateProfileFields(
                            mapOf(
                                "xp" to (current.xp + 25L),
                                "dailyMinutesCompleted" to (current.dailyMinutesCompleted + 3L).coerceAtMost(1440L)
                            )
                        )
                    }
                }
            }.onFailure { err ->
                _statusBanner.value = "Erreur IA : ${err.localizedMessage ?: "Vérifiez votre connexion."}"
            }
            _isAiLoading.value = false
        }
    }

    fun evaluateUserWriting(userText: String, promptTitle: String, targetCefr: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiTutorService.evaluateWriting(
                userText = userText,
                promptTitle = promptTitle,
                targetCefr = targetCefr,
                uiLanguage = _uiLanguage.value
            )
            result.onSuccess { evaluation ->
                _writingResult.value = evaluation
                recordModuleActivity(
                    moduleType = "writing",
                    itemId = "wrt_${System.currentTimeMillis()}",
                    title = promptTitle,
                    cefrLevel = evaluation.cefrEstimate,
                    score = evaluation.score,
                    xpEarned = 45,
                    feedback = evaluation.grammarAndSpellingFeedback
                )
            }.onFailure { err ->
                _statusBanner.value = "Erreur d'évaluation IA : ${err.localizedMessage}"
            }
            _isAiLoading.value = false
        }
    }

    fun generateAiExercise(topic: String, cefrLevel: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiTutorService.generateCustomExercise(
                topic = topic.ifBlank { "Phrasal Verbs & Conditionals" },
                cefrLevel = cefrLevel,
                uiLanguage = _uiLanguage.value
            )
            res.onSuccess { quiz ->
                _generatedQuiz.value = quiz
            }.onFailure { err ->
                _statusBanner.value = "Génération d'exercice échouée : ${err.localizedMessage}"
            }
            _isAiLoading.value = false
        }
    }

    // --- Offline Mode (Section 11) ---

    fun downloadOfflinePack(packId: String, title: String, cefrLevel: String, moduleType: String) {
        viewModelScope.launch {
            offlineDao.savePack(
                OfflinePackEntity(
                    packId = packId,
                    title = title,
                    cefrLevel = cefrLevel,
                    moduleType = moduleType,
                    lessonsCount = 6,
                    flashcardsCount = 25,
                    exercisesCount = 15,
                    summaryFr = "Pack complet téléchargé pour révision hors ligne (leçons, flashcards SRS et quiz).",
                    summaryEn = "Complete pack downloaded for offline study (lessons, SRS flashcards, and quizzes).",
                    summaryAr = "تم تنزيل الحزمة الكاملة للدراسة دون اتصال بالإنترنت."
                )
            )
            _statusBanner.value = "💾 Pack '$title' disponible hors ligne !"
        }
    }

    fun removeOfflinePack(packId: String) {
        viewModelScope.launch {
            offlineDao.removePack(packId)
            _statusBanner.value = "Pack hors ligne supprimé."
        }
    }

    fun syncOfflineQueueToCloud() {
        if (isGuestMode) {
            _statusBanner.value = "🔐 Connectez-vous avec Google pour synchroniser vos données locales vers le cloud !"
            return
        }
        viewModelScope.launch {
            val pending = offlineDao.getPendingScoresList()
            if (pending.isEmpty()) {
                _statusBanner.value = "☁️ Toutes vos données hors ligne sont déjà synchronisées."
                return@launch
            }
            var syncedCount = 0
            for (item in pending) {
                val res = repository.recordProgress(
                    moduleType = item.moduleType,
                    itemId = item.itemId,
                    title = item.title,
                    cefrLevel = item.cefrLevel,
                    score = item.score.toLong(),
                    maxScore = item.maxScore.toLong(),
                    xpEarned = item.xpEarned.toLong(),
                    feedback = "Synced from Offline / Guest Mode"
                )
                if (res.isSuccess) {
                    offlineDao.markScoreSynced(item.localId)
                    syncedCount++
                }
            }
            _statusBanner.value = "☁️ $syncedCount activité(s) synchronisée(s) vers le cloud !"
        }
    }

    // --- Admin Panel (Section 13) ---

    fun publishAdminCatalogItem(
        itemType: String,
        title: String,
        subtitle: String,
        cefrLevel: String,
        contentBody: String,
        translationFr: String,
        translationAr: String,
        quizQuestion: String,
        quizOptionsCsv: String,
        quizAnswer: String
    ) {
        if (isGuestMode) {
            _statusBanner.value = "🔐 Connexion requise pour publier dans le catalogue cloud."
            return
        }
        viewModelScope.launch {
            val res = repository.createCatalogItem(
                itemType = itemType,
                title = title,
                subtitle = subtitle,
                cefrLevel = cefrLevel,
                contentBody = contentBody,
                translationFr = translationFr,
                translationAr = translationAr,
                quizQuestion = quizQuestion,
                quizOptionsCsv = quizOptionsCsv,
                quizAnswer = quizAnswer,
                published = true
            )
            if (res.isSuccess) {
                _statusBanner.value = "Contenu '$title' publié dans le catalogue cloud !"
            } else {
                _statusBanner.value = "Erreur publication : ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun importCsvCatalogBatch(csvRaw: String) {
        if (isGuestMode) {
            _statusBanner.value = "🔐 Connexion requise pour importer un lot CSV dans Firestore."
            return
        }
        viewModelScope.launch {
            val lines = csvRaw.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") }
            var imported = 0
            for (line in lines) {
                val parts = line.split(";", ",")
                if (parts.size >= 4) {
                    val word = parts[0].trim()
                    val fr = parts[1].trim()
                    val ar = parts[2].trim()
                    val level = parts[3].trim().uppercase().let {
                        if (it in listOf("A1", "A2", "B1", "B2", "C1", "C2")) it else "B1"
                    }
                    val example = parts.getOrNull(4)?.trim() ?: "Example with $word."
                    val res = repository.createCatalogItem(
                        itemType = "vocabulary",
                        title = word,
                        subtitle = "CSV Import ($level)",
                        cefrLevel = level,
                        contentBody = example,
                        translationFr = fr,
                        translationAr = ar,
                        quizQuestion = "What is the translation of '$word'?",
                        quizOptionsCsv = "$fr,Autre sens,Contraire,Inconnu",
                        quizAnswer = fr,
                        published = true
                    )
                    if (res.isSuccess) imported++
                }
            }
            _statusBanner.value = "$imported entrée(s) CSV importée(s) et publiée(s) !"
        }
    }

    fun deleteAdminCatalogItem(itemId: String) {
        if (isGuestMode) return
        viewModelScope.launch {
            repository.deleteCatalogItem(itemId)
            _statusBanner.value = "Élément supprimé du catalogue."
        }
    }

    // --- User-to-User Community & Tandem Chat Actions ---

    fun selectCommunityChannel(roomId: String) {
        _selectedTandemPeer.value = null
        _selectedCommunityRoomId.value = roomId
    }

    fun startTandemChatWithPeer(peerUid: String, peerName: String) {
        if (peerUid.isBlank() || peerUid == currentUserId) return
        val cleanA = minOf(currentUserId, peerUid).replace(Regex("[^a-zA-Z0-9_-]"), "").take(48)
        val cleanB = maxOf(currentUserId, peerUid).replace(Regex("[^a-zA-Z0-9_-]"), "").take(48)
        val dmRoomId = "dm_${cleanA}_${cleanB}"
        _selectedTandemPeer.value = peerUid to peerName
        _selectedCommunityRoomId.value = dmRoomId
    }

    fun sendCommunityChatMessage(content: String) {
        if (content.isBlank()) return
        if (isGuestMode) {
            _statusBanner.value = "🔐 Créez un compte gratuit pour discuter en direct avec les autres apprenants !"
            return
        }
        viewModelScope.launch {
            val current = (profileState.value as? DataUiState.Success)?.data
            val senderName = current?.displayName ?: initialDisplayName
            val senderCefr = current?.cefrLevel ?: "A1"
            val roomId = _selectedCommunityRoomId.value
            val tandemPeer = _selectedTandemPeer.value

            val res = repository.sendCommunityMessage(
                senderName = senderName,
                senderCefr = senderCefr,
                roomId = roomId,
                content = content,
                recipientId = tandemPeer?.first ?: "",
                recipientName = tandemPeer?.second ?: ""
            )
            if (res.isSuccess) {
                _aiDraftSuggestion.value = null
                if (current != null) {
                    val newXp = current.xp + 10L
                    repository.updateProfileFields(
                        mapOf(
                            "xp" to newXp,
                            "dailyMinutesCompleted" to (current.dailyMinutesCompleted + 2L).coerceAtMost(1440L)
                        )
                    )
                    repository.syncLeaderboardEntry(
                        displayName = current.displayName,
                        cefrLevel = current.cefrLevel,
                        xp = newXp,
                        streakDays = current.streakDays,
                        badgesCount = EnglishCurriculum.badges.count { newXp >= it.requiredXp }.toLong().coerceAtLeast(1L)
                    )
                }
            } else {
                _statusBanner.value = "Erreur d'envoi : ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun submitPeerGrammarCorrection(messageId: String, correction: String) {
        if (correction.isBlank() || isGuestMode) return
        viewModelScope.launch {
            val current = (profileState.value as? DataUiState.Success)?.data
            val correctorName = current?.displayName ?: initialDisplayName
            val res = repository.submitPeerCorrection(
                messageId = messageId,
                correctionSuggestion = correction,
                correctedByName = correctorName
            )
            if (res.isSuccess) {
                if (current != null) {
                    val newXp = current.xp + 15L
                    repository.updateProfileFields(mapOf("xp" to newXp))
                }
                _statusBanner.value = "✏️ Correction partagée avec la communauté (+15 XP ⭐) !"
            }
        }
    }

    fun likeCommunityMessage(messageId: String, currentLikes: Long) {
        if (isGuestMode) return
        viewModelScope.launch {
            repository.likeCommunityMessage(messageId, currentLikes)
        }
    }

    fun deleteCommunityMessage(messageId: String) {
        if (isGuestMode) return
        viewModelScope.launch {
            repository.deleteCommunityMessage(messageId)
            _statusBanner.value = "Message supprimé."
        }
    }

    fun checkMessageDraftWithAi(draft: String) {
        if (draft.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val current = (profileState.value as? DataUiState.Success)?.data
            val cefr = current?.cefrLevel ?: "B1"
            val result = aiTutorService.askTutor(
                userMessage = "Please check and improve this message before I send it to another learner: \"$draft\"",
                cefrLevel = cefr,
                scenario = "Grammar Coach",
                uiLanguage = _uiLanguage.value,
                recentHistory = emptyList()
            )
            result.onSuccess { reply ->
                _aiDraftSuggestion.value = reply.grammarCorrection.ifBlank { reply.englishResponse }
            }.onFailure { err ->
                _statusBanner.value = "Vérification IA indisponible : ${err.localizedMessage}"
            }
            _isAiLoading.value = false
        }
    }

    fun clearAiDraftSuggestion() {
        _aiDraftSuggestion.value = null
    }
}
