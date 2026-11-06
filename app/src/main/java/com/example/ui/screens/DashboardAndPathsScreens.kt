package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.curriculum.EnglishCurriculum
import com.example.data.curriculum.GamifiedUnitNode
import com.example.data.curriculum.LearningTrack
import com.example.data.curriculum.LocalizedStrings
import com.example.data.model.LearningProgress
import com.example.data.model.UserProfile
import com.example.ui.theme.FeatherGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.VibrantOrange
import com.example.ui.viewmodel.CelebrationEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTabScreen(
    profile: UserProfile,
    strings: LocalizedStrings,
    recentProgress: List<LearningProgress>,
    isGuestMode: Boolean = false,
    heartsCount: Int = 5,
    completedUnitIds: Set<String> = emptySet(),
    onRefillHearts: () -> Unit = {},
    onConsumeHeart: () -> Unit = {},
    onCompleteGamifiedUnit: (unitId: String, title: String, cefr: String, xp: Int, stars: Int) -> Unit = { _, _, _, _, _ -> },
    onSpeakText: (String) -> Unit = {},
    onOpenSaveProgressAuth: () -> Unit = {},
    onOpenPlacementTest: () -> Unit,
    onOpenLearningPaths: () -> Unit,
    onNavigateToModule: (String) -> Unit,
    onOpenTutor: () -> Unit,
    onOpenCommunityChat: () -> Unit = {}
) {
    val units = EnglishCurriculum.gamifiedUnits
    var activeUnitModal by remember { mutableStateOf<GamifiedUnitNode?>(null) }

    // Determine next unlocked unit for "Continue Learning"
    val nextUnitToPlay = units.firstOrNull { it.id !in completedUnitIds } ?: units.first()
    val overallPathRatio = if (units.isEmpty()) 0f else (completedUnitIds.size.toFloat() / units.size.toFloat()).coerceIn(0f, 1f)

    if (activeUnitModal != null) {
        GamifiedUnitInteractiveDialog(
            unit = activeUnitModal!!,
            heartsCount = heartsCount,
            onSpeakText = onSpeakText,
            onMistake = onConsumeHeart,
            onRefillHearts = onRefillHearts,
            onCompleteUnit = { stars ->
                val u = activeUnitModal!!
                onCompleteGamifiedUnit(u.id, u.titleEn, u.cefrLevel, u.xpReward, stars)
                activeUnitModal = null
            },
            onDismiss = { activeUnitModal = null }
        )
    }

    val moduleCards = remember {
        listOf(
            Triple("vocabulary", "🧠 Vocabulary & Flashcards", "Mots du quotidien, idiomes, audio & répétition espacée SRS"),
            Triple("grammar", "📖 Grammar & Tenses", "Tous les temps, conditionnels, voix passive & exercices corrigés"),
            Triple("listening", "🎧 Listening & Podcasts", "Dialogues audio, vitesse 0.75x-1.5x & dictées interactives"),
            Triple("speaking", "🎤 Speaking UK/US", "Reconnaissance vocale, prononciation & score de fluidité"),
            Triple("reading", "📖 Reading & Stories", "Histoires, articles pro & glossaire tactile instantané"),
            Triple("writing", "✍️ Writing & AI Coach", "Rédaction d'emails et essais corrigés par Gemini")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_learning_path_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER AREA: User Profile, Daily Streak (🔥), XP Count (⭐), Hearts (❤️) & Overall Path Ring
        item(key = "header_user_profile_streak_xp") {
            DashboardProfileHeaderCard(
                profile = profile,
                isGuestMode = isGuestMode,
                heartsCount = heartsCount,
                overallPathRatio = overallPathRatio,
                completedUnitsCount = completedUnitIds.size,
                totalUnitsCount = units.size,
                nextUnitToPlay = nextUnitToPlay,
                onRefillHearts = onRefillHearts,
                onContinueLearning = { activeUnitModal = nextUnitToPlay },
                onOpenPlacementTest = onOpenPlacementTest
            )
        }

        // 2. Guest Mode Non-Intrusive Conversion Card & Permanent "💾 Sauvegarder ma progression" Button
        if (isGuestMode) {
            item(key = "guest_conversion_banner") {
                val guestPromptText = when {
                    profile.lessonsCompleted >= 3L ->
                        "🎉 Tu progresses ! Crée un compte pour sauvegarder tes résultats."
                    profile.streakDays >= 5L ->
                        "🔥 Ne perds pas ta progression, synchronise ton compte."
                    else ->
                        "👤 Mode Invité actif : tes leçons, XP et flashcards sont enregistrés sur cet appareil."
                }
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = guestPromptText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(
                            onClick = onOpenSaveProgressAuth,
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_guest_progress_button")
                        ) {
                            Text(
                                text = "💾 Sauvegarder ma progression (Compte Cloud)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Daily Goal & Quests Card (🎯 Goals & 🎁 Rewards)
        item(key = "daily_goal_card") {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                val goalRatio = (profile.dailyMinutesCompleted.toFloat() / profile.dailyGoalMinutes.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Objectif journalier : ${profile.dailyMinutesCompleted} / ${profile.dailyGoalMinutes} min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = SunnyYellow.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "🎁 +25 XP Bonus",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { goalRatio },
                        color = FeatherGreen,
                        trackColor = FeatherGreen.copy(alpha = 0.18f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🧠 ${profile.wordsLearned} mots appris",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "📚 ${profile.lessonsCompleted} leçons terminées",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Learning Path Section Header
        item(key = "learning_path_section_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🗺️ Parcours d'Apprentissage Gamifié",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Complétez chaque unité pour faire progresser son cercle vert (#58CC02) et débloquer la suivante !",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onOpenLearningPaths,
                    modifier = Modifier.testTag("dashboard_paths_button")
                ) {
                    Text("🏆 10 Parcours", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. VERTICAL SCROLLING LEARNING PATH: Material 3 Cards for Units with CircularProgressIndicator (#58CC02)
        itemsIndexed(
            items = units,
            key = { _, unit -> unit.id }
        ) { index, unit ->
            val isCompleted = unit.id in completedUnitIds
            val previousCompleted = index == 0 || index == 1 || units[index - 1].id in completedUnitIds
            val isUnlocked = isCompleted || previousCompleted || (profile.xp >= (index * 80L))
            val isCurrentActiveUnit = unit.id == nextUnitToPlay.id

            // Compute unit completion ratio (0f..1f) for the #58CC02 CircularProgressIndicator
            val unitCompletionRatio = when {
                isCompleted -> 1.0f
                isCurrentActiveUnit -> {
                    val partialFromLessons = ((profile.lessonsCompleted % 3L).toFloat() / 3f).coerceIn(0.33f, 0.67f)
                    if (profile.lessonsCompleted > 0L) partialFromLessons else 0.25f
                }
                isUnlocked -> 0.15f
                else -> 0.0f
            }

            LearningPathUnitCard(
                index = index,
                totalUnits = units.size,
                unit = unit,
                isCompleted = isCompleted,
                isUnlocked = isUnlocked,
                isCurrentActiveUnit = isCurrentActiveUnit,
                completionRatio = unitCompletionRatio,
                onClickUnit = {
                    if (isUnlocked) {
                        activeUnitModal = unit
                    }
                }
            )
        }

        // 6. AI Tutor & Community Conversation Quick Launch Card
        item(key = "ai_tutor_and_community_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🤖 AI Tutor 24/7 & 💬 Conversation Communautaire",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Pratiquez l'oral avec Prof. Victoria ou échangez avec d'autres apprenants en Tandem 1-to-1 !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenTutor,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_ai_tutor_button")
                        ) {
                            Text("🤖 AI Tutor")
                        }
                        OutlinedButton(
                            onClick = onOpenCommunityChat,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_community_chat_button")
                        ) {
                            Text("💬 Salon & Tandem")
                        }
                    }
                }
            }
        }

        // 7. Quick Access Grid to the 6 Pedagogical Modules with Explicit Emojis
        item(key = "modules_section_header") {
            Text(
                text = "📚 Modules d'Apprentissage (A1 → C2)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        itemsIndexed(
            items = moduleCards,
            key = { _, item -> "module_${item.first}" }
        ) { _, (key, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToModule(key) }
                    .testTag("open_module_$key"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 8. Recent History
        if (recentProgress.isNotEmpty()) {
            item(key = "recent_history_section") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🏆 Historique Récent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    recentProgress.take(4).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${item.moduleType.uppercase()} • Niveau ${item.cefrLevel} • Score ${item.score}/${item.maxScore}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    "⭐ +${item.xpEarned} XP",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header area displaying User Profile, Daily Streak (🔥), XP Count (⭐), Hearts (❤️),
 * overall learning path circular progress in #58CC02, and quick "Continue Learning" action.
 */
@Composable
private fun DashboardProfileHeaderCard(
    profile: UserProfile,
    isGuestMode: Boolean,
    heartsCount: Int,
    overallPathRatio: Float,
    completedUnitsCount: Int,
    totalUnitsCount: Int,
    nextUnitToPlay: GamifiedUnitNode,
    onRefillHearts: () -> Unit,
    onContinueLearning: () -> Unit,
    onOpenPlacementTest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Top HUD Strip: User Profile Badge | 🔥 Daily Streak | ⭐ XP Count | ❤️ Hearts
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_top_hud_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GamifiedHudPill(
                    emoji = "🔥",
                    text = "${profile.streakDays}j Streak",
                    badgeColor = VibrantOrange.copy(alpha = 0.16f),
                    textColor = VibrantOrange
                )
                GamifiedHudPill(
                    emoji = "⭐",
                    text = "${profile.xp} XP",
                    badgeColor = SunnyYellow.copy(alpha = 0.22f),
                    textColor = Color(0xFFB45309)
                )
                GamifiedHudPill(
                    emoji = "❤️",
                    text = "$heartsCount/5 Vies",
                    badgeColor = HeartRed.copy(alpha = 0.15f),
                    textColor = HeartRed,
                    onClick = onRefillHearts
                )
                GamifiedHudPill(
                    emoji = "🎯",
                    text = profile.cefrLevel,
                    badgeColor = SkyBlue.copy(alpha = 0.18f),
                    textColor = SkyBlue
                )
            }
        }

        // User Profile & Barnaby Mascot Hero Card with Overall Path CircularProgressIndicator (#58CC02)
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_profile_header_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar wrapped with #58CC02 CircularProgressIndicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(86.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { overallPathRatio.coerceAtLeast(0.12f) },
                            color = FeatherGreen,
                            trackColor = Color.White.copy(alpha = 0.55f),
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.size(86.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.img_mascot_barnaby_1793914916960),
                            contentDescription = "Barnaby Mascot English Master AI",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "👋 Hello! Welcome back,",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                color = FeatherGreen.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isGuestMode) "👤 Invité" else "🔐 Cloud",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "🔥 Série : ${profile.streakDays} jours • ⭐ Total : ${profile.xp} XP • 🗺️ $completedUnitsCount/$totalUnitsCount unités",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "🦊 Barnaby : \"Prêt pour ${nextUnitToPlay.titleEn} ? Gagne +${nextUnitToPlay.xpReward} XP ⭐ !\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Level & XP Progress Bar in Brand Green (#58CC02)
                val currentLevelNum = (profile.xp / 200L) + 1L
                val xpInCurrentLevel = (profile.xp % 200L).toFloat() / 200f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏆 Niveau $currentLevelNum • CECRL ${profile.cefrLevel}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${profile.xp % 200L} / 200 ⭐ XP",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    LinearProgressIndicator(
                        progress = { xpInCurrentLevel.coerceIn(0.08f, 1f) },
                        color = FeatherGreen,
                        trackColor = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                }

                // Prominent "Continue Learning" Button + Placement Test
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onContinueLearning,
                        colors = ButtonDefaults.buttonColors(containerColor = FeatherGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("continue_learning_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Continue Learning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenPlacementTest,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("dashboard_placement_button")
                    ) {
                        Text("🎯 Test A1-C2")
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Unit Card for the vertical learning path featuring dual #58CC02 CircularProgressIndicators
 * (around the unit badge and as a right-hand completion percentage ring).
 */
@Composable
private fun LearningPathUnitCard(
    index: Int,
    totalUnits: Int,
    unit: GamifiedUnitNode,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isCurrentActiveUnit: Boolean,
    completionRatio: Float,
    onClickUnit: () -> Unit
) {
    val unitAccentColor = Color(unit.accentColorHex)
    val animatedCompletion by animateFloatAsState(
        targetValue = completionRatio.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "unitCircularProgress"
    )
    val completionPercent = (animatedCompletion * 100).toInt()

    // Subtle winding offset for the gamified learning path
    val horizontalBias = when (index % 4) {
        1 -> 16.dp
        3 -> (-16).dp
        else -> 0.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (horizontalBias > 0.dp) horizontalBias else 0.dp,
                end = if (horizontalBias < 0.dp) (-horizontalBias.value).dp else 0.dp
            )
    ) {
        ElevatedCard(
            onClick = onClickUnit,
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (isCurrentActiveUnit) 6.dp else 2.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = when {
                    isCompleted -> FeatherGreen.copy(alpha = 0.12f)
                    isUnlocked -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCurrentActiveUnit) {
                        Modifier.border(2.5.dp, FeatherGreen, RoundedCornerShape(24.dp))
                    } else {
                        Modifier
                    }
                )
                .testTag("gamified_unit_${unit.unitNumber}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left: Unit Icon Node wrapped in a #58CC02 CircularProgressIndicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedCompletion },
                        color = FeatherGreen,
                        trackColor = FeatherGreen.copy(alpha = 0.20f),
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.size(68.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) unitAccentColor else Color.Gray.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUnlocked) {
                            Text(
                                text = unit.emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked Unit",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Middle: Unit Title, CEFR badge, description, stars & XP reward
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = unit.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = if (isUnlocked) unitAccentColor else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = unit.cefrLevel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = unit.subtitleFr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                isCompleted -> "⭐⭐⭐ Complété"
                                isCurrentActiveUnit -> "🟢 En cours (${unit.lessons.size} leçons)"
                                isUnlocked -> "⭐☆☆ Débloqué (${unit.lessons.size} leçons)"
                                else -> "🔒 Verrouillé"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCompleted -> FeatherGreen
                                isCurrentActiveUnit -> FeatherGreen
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "• +${unit.xpReward} XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Right: Dedicated CircularProgressIndicator (#58CC02) showing completion %
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("unit_progress_${unit.unitNumber}")
                ) {
                    CircularProgressIndicator(
                        progress = { animatedCompletion },
                        color = FeatherGreen,
                        trackColor = FeatherGreen.copy(alpha = 0.18f),
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.size(54.dp)
                    )
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "100% Completed",
                            tint = FeatherGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Text(
                            text = "$completionPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUnlocked) FeatherGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Vertical path connector line between units in #58CC02 when completed
        if (index < totalUnits - 1) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isCompleted) FeatherGreen else FeatherGreen.copy(alpha = 0.22f)
                    )
            )
        }
    }
}

@Composable
private fun GamifiedHudPill(
    emoji: String,
    text: String,
    badgeColor: Color,
    textColor: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = badgeColor,
        shape = RoundedCornerShape(50),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun GamifiedUnitInteractiveDialog(
    unit: GamifiedUnitNode,
    heartsCount: Int,
    onSpeakText: (String) -> Unit,
    onMistake: () -> Unit,
    onRefillHearts: () -> Unit,
    onCompleteUnit: (starsEarned: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var lessonIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }

    val currentLesson = unit.lessons.getOrNull(lessonIdx) ?: unit.lessons.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = unit.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "❤️ $heartsCount/5",
                        color = HeartRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (lessonIdx + 1).toFloat() / unit.lessons.size.toFloat() },
                    color = FeatherGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${currentLesson.title} (${lessonIdx + 1}/${unit.lessons.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎧 \"${currentLesson.audioPhrase}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onSpeakText(currentLesson.audioPhrase) }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Écouter l'audio")
                        }
                    }
                }

                Text(
                    text = currentLesson.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                currentLesson.options.forEachIndexed { idx, opt ->
                    Card(
                        onClick = { if (!showFeedback) selectedOption = opt },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                showFeedback && opt == currentLesson.correctAnswer ->
                                    FeatherGreen.copy(alpha = 0.25f)
                                selectedOption == opt ->
                                    MaterialTheme.colorScheme.primaryContainer
                                else ->
                                    MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unit_lesson_opt_$idx")
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (showFeedback) {
                    val isCorrect = selectedOption == currentLesson.correctAnswer
                    Surface(
                        color = if (isCorrect) FeatherGreen.copy(alpha = 0.18f) else HeartRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isCorrect) "🎉 Bravo ! Bonne réponse !" else "💡 Réponse : ${currentLesson.correctAnswer}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentLesson.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showFeedback) {
                Button(
                    onClick = {
                        if (selectedOption != null) {
                            showFeedback = true
                            if (selectedOption == currentLesson.correctAnswer) {
                                correctAnswersCount++
                            } else {
                                onMistake()
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier.testTag("unit_validate_step_button")
                ) {
                    Text("Valider")
                }
            } else {
                Button(
                    onClick = {
                        if (lessonIdx + 1 < unit.lessons.size) {
                            lessonIdx++
                            selectedOption = null
                            showFeedback = false
                        } else {
                            val stars = correctAnswersCount.coerceIn(1, 3)
                            onCompleteUnit(stars)
                        }
                    },
                    modifier = Modifier.testTag("unit_next_step_button")
                ) {
                    Text(if (lessonIdx + 1 < unit.lessons.size) "Continuer ➡️" else "Terminer (+${unit.xpReward} XP ⭐)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun CelebrationModalOverlay(
    event: CelebrationEvent,
    onDismiss: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "celebration_scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎊 ${event.emoji} 🌟", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_barnaby_1793914916960),
                    contentDescription = "Barnaby Celebrating",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(3.dp, SunnyYellow, CircleShape)
                )
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = SunnyYellow.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "⭐ +${event.xpBonus} XP Gagnés !",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FeatherGreen, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_celebration_button")
            ) {
                Text("🎉 Super, continuer !")
            }
        }
    )
}

@Composable
fun LearningPathsScreen(
    profile: UserProfile,
    onSelectTrack: (LearningTrack) -> Unit,
    onCompleteTrackUnit: (LearningTrack, String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var certificateTrack by remember { mutableStateOf<LearningTrack?>(null) }

    if (certificateTrack != null) {
        val track = certificateTrack!!
        AlertDialog(
            onDismissRequest = { certificateTrack = null },
            title = { Text("🎓 Certificat Officiel de Niveau") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("English Master AI Academy certifie que :", style = MaterialTheme.typography.bodyMedium)
                    Text(profile.displayName, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("a validé les compétences du parcours :")
                    Text("${track.titleFr} (${track.cefrRange})", fontWeight = FontWeight.Bold)
                    Text("Badge décerné : 🏆 ${track.badgeTitle}")
                    Text("Score XP cumulé : ⭐ ${profile.xp} XP • Niveau CECRL : ${profile.cefrLevel}")
                }
            },
            confirmButton = {
                Button(onClick = { certificateTrack = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_paths_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("🏆 10 Parcours Structurés & Certifiants", style = MaterialTheme.typography.titleLarge)
                Text("IELTS • TOEFL • TOEIC • Business • Voyage • Académique", style = MaterialTheme.typography.bodyMedium)
            }
        }

        EnglishCurriculum.learningTracks.forEach { track ->
            val isSelected = track.id == profile.selectedTrackId
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.titleFr, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${track.category} • Niveaux ${track.cefrRange} • ${track.unitsCount} unités (${track.lessonsCount} leçons)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = { onSelectTrack(track) },
                            label = { Text(if (isSelected) "Actif ✓" else "Choisir") }
                        )
                    }

                    track.units.forEachIndexed { idx, unitName ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Unité ${idx + 1} : $unitName", style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { onCompleteTrackUnit(track, unitName) }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Valider (+30 XP ⭐)")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Badge : ${track.badgeTitle}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        OutlinedButton(onClick = { certificateTrack = track }) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Certificat")
                        }
                    }
                }
            }
        }
    }
}
