package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.GeneratedQuizItem
import com.example.data.ai.TutorStructuredReply
import com.example.data.curriculum.EnglishCurriculum
import com.example.data.model.LeaderboardEntry
import com.example.data.model.TutorMessage
import com.example.data.model.UserProfile
import com.example.ui.theme.FeatherGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.VibrantOrange
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTutorTabScreen(
    profile: UserProfile,
    messages: List<TutorMessage>,
    latestReply: TutorStructuredReply?,
    isAiLoading: Boolean,
    onSendMessage: (text: String, scenario: String) -> Unit,
    onSpeakText: (String) -> Unit
) {
    val scenarios = listOf(
        "free" to "💬 Conversation Libre",
        "job_interview" to "💼 Entretien d'Embauche",
        "business" to "📊 Business English",
        "travel" to "✈️ Anglais Voyage",
        "daily" to "🏠 Anglais Quotidien",
        "grammar_coach" to "📖 Coach Grammatical"
    )
    var selectedScenario by remember { mutableStateOf("free") }
    var messageInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tutor Header Card with Avatar
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_tutor_avatar_1793908847637),
                    contentDescription = "Professor Victoria Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("🤖 Prof. Victoria • AI English Tutor 24/7", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Adapté à votre niveau ${profile.cefrLevel} • Correction instantanée & explications en FR/AR/EN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Scenario selector chips
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scenarios.forEach { (id, label) ->
                FilterChip(
                    selected = selectedScenario == id,
                    onClick = { selectedScenario = id },
                    label = { Text(label) },
                    modifier = Modifier.testTag("tutor_scenario_$id")
                )
            }
        }

        // Starter conversation prompts
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {
                    val prompt = when (selectedScenario) {
                        "job_interview" -> "Hello Professor! Can we practice a software engineering job interview?"
                        "business" -> "How can I politely negotiate a contract deadline with a client?"
                        "travel" -> "I just landed at London Heathrow and my luggage is delayed."
                        else -> "Hi Victoria! Let's practice speaking English at level ${profile.cefrLevel}."
                    }
                    onSendMessage(prompt, selectedScenario)
                },
                label = { Text("✨ Lancer une simulation") },
                modifier = Modifier.testTag("tutor_quick_start_chip")
            )
            if (latestReply != null && latestReply.suggestedFollowUp.isNotBlank()) {
                AssistChip(
                    onClick = { onSendMessage(latestReply.suggestedFollowUp, selectedScenario) },
                    label = { Text("💬 Répondre : \"${latestReply.suggestedFollowUp.take(28)}...\"") }
                )
            }
        }

        // Conversation Messages
        if (messages.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "👩‍🏫 Welcome ${profile.displayName}! I am Professor Victoria, your 24/7 private English tutor.",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Write any sentence in English below (or pick a scenario like Job Interview or Business English). I will reply naturally, correct any mistakes instantly, and explain the grammar rule in your preferred language!"
                    )
                }
            }
        } else {
            messages.sortedBy { it.createdAt?.seconds ?: 0L }.takeLast(12).forEach { msg ->
                val isUser = msg.role == "user"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUser) "👤 Vous (${msg.scenario})" else "🤖 Prof. Victoria (AI Tutor)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!isUser) {
                                IconButton(
                                    onClick = { onSpeakText(msg.content) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
                                }
                            }
                        }
                        Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                        if (!isUser && msg.correction.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("✏️ Correction : ${msg.correction}", fontWeight = FontWeight.Bold)
                                    if (msg.explanation.isNotBlank()) {
                                        Text("📚 Explication : ${msg.explanation}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAiLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Text("🤖 Prof. Victoria analyse votre phrase et rédige une explication...")
            }
        }

        // Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                label = { Text("✍️ Écrivez en anglais à votre professeur IA...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_message_input")
            )
            Button(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        onSendMessage(messageInput, selectedScenario)
                        messageInput = ""
                    }
                },
                enabled = !isAiLoading && messageInput.isNotBlank(),
                modifier = Modifier
                    .height(54.dp)
                    .testTag("tutor_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeAndGamificationTabScreen(
    profile: UserProfile,
    leaderboard: List<LeaderboardEntry>,
    generatedQuiz: GeneratedQuizItem?,
    isAiLoading: Boolean,
    heartsCount: Int = 5,
    onMistakeConsumeHeart: () -> Unit = {},
    onSpeakText: (String) -> Unit,
    onRecordExerciseResult: (itemId: String, title: String, cefr: String, score: Int, xp: Int, feedback: String) -> Unit,
    onGenerateAiExercise: (topic: String, cefr: String) -> Unit,
    onStartTandemWithUser: (userId: String, displayName: String) -> Unit = { _, _ -> }
) {
    val exercises = EnglishCurriculum.multiFormatExercises
    var currentIdx by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var timedModeActive by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(45) }
    var aiTopicInput by remember { mutableStateOf("Business Negotiations & Conditionals") }
    var selectedAiAnswer by remember(generatedQuiz) { mutableStateOf<String?>(null) }

    LaunchedEffect(timedModeActive, currentIdx) {
        if (timedModeActive) {
            timerSeconds = 45
            while (timerSeconds > 0 && !showFeedback) {
                delay(1000L)
                timerSeconds--
            }
        }
    }

    val currentEx = exercises[currentIdx % exercises.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Hearts + Timed Quiz Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("🎮 Games & Quiz Multi-Formats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("🎯 QCM • Phrases • Ordre des mots • Traduction • Dictée • ❤️ $heartsCount/5 Vies", style = MaterialTheme.typography.bodyMedium)
            }
            FilterChip(
                selected = timedModeActive,
                onClick = { timedModeActive = !timedModeActive },
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(if (timedModeActive) "⏱️ ${timerSeconds}s" else "⏱️ Chrono") }
            )
        }

        // Multi-Format Exercise Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(onClick = {}, label = { Text("🎮 Format : ${currentEx.format}") })
                    AssistChip(onClick = {}, label = { Text("🎯 Niveau ${currentEx.cefrLevel}") })
                }

                Text(currentEx.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (currentEx.audioPhrase != null) {
                    OutlinedButton(onClick = { onSpeakText(currentEx.audioPhrase) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🎧 Écouter l'audio de l'exercice")
                    }
                }

                currentEx.options.forEachIndexed { i, option ->
                    Card(
                        onClick = { if (!showFeedback) selectedAnswer = option },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                showFeedback && option == currentEx.correctAnswer ->
                                    FeatherGreen.copy(alpha = 0.25f)
                                selectedAnswer == option ->
                                    MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_option_$i")
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                if (!showFeedback) {
                    Button(
                        onClick = {
                            if (selectedAnswer != null) {
                                showFeedback = true
                                val ok = selectedAnswer == currentEx.correctAnswer
                                if (!ok) {
                                    onMistakeConsumeHeart()
                                }
                                onRecordExerciseResult(
                                    currentEx.id,
                                    "Quiz ${currentEx.format}",
                                    currentEx.cefrLevel,
                                    if (ok) 100 else 40,
                                    if (ok) 30 else 10,
                                    currentEx.explanationFr
                                )
                            }
                        },
                        enabled = selectedAnswer != null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("quiz_validate_button")
                    ) {
                        Text("✅ Valider ma réponse", fontWeight = FontWeight.Bold)
                    }
                } else {
                    val isCorrect = selectedAnswer == currentEx.correctAnswer
                    Surface(
                        color = if (isCorrect) FeatherGreen.copy(alpha = 0.18f) else HeartRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isCorrect) "🎉 Bonne réponse ! Score : 100/100 (+30 XP ⭐)" else "❌ Réponse incorrecte (-1 ❤️) : $selectedAnswer",
                                fontWeight = FontWeight.Bold
                            )
                            Text("✔ Bonne réponse : ${currentEx.correctAnswer}", fontWeight = FontWeight.Bold)
                            Text("📚 Explication : ${currentEx.explanationFr}")
                            Text("💡 Conseil de révision : ${currentEx.revisionTip}", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Button(
                        onClick = {
                            currentIdx++
                            selectedAnswer = null
                            showFeedback = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text("➡️ Exercice suivant", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dynamic AI Exercise Generator Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🤖 Générateur d'Exercices Personnalisés par IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = aiTopicInput,
                    onValueChange = { aiTopicInput = it },
                    label = { Text("🎯 Sujet (ex: Medical English, Phrasal Verbs, IELTS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onGenerateAiExercise(aiTopicInput, profile.cefrLevel) },
                    enabled = !isAiLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("generate_ai_quiz_button")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🤖 Création de l'exercice par Gemini...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Générer un Quiz sur mesure (${profile.cefrLevel})")
                    }
                }

                if (generatedQuiz != null) {
                    Text(generatedQuiz.question, fontWeight = FontWeight.Bold)
                    generatedQuiz.options.forEach { opt ->
                        FilterChip(
                            selected = selectedAiAnswer == opt,
                            onClick = { selectedAiAnswer = opt },
                            label = { Text(opt) }
                        )
                    }
                    if (selectedAiAnswer != null) {
                        val ok = selectedAiAnswer == generatedQuiz.correctAnswer
                        Text(
                            text = if (ok) "🎉 Bravo ! ${generatedQuiz.explanation}" else "❌ Réponse : ${generatedQuiz.correctAnswer}. ${generatedQuiz.explanation}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RankingAndAchievementsTabScreen(
    profile: UserProfile,
    leaderboard: List<LeaderboardEntry>,
    isGuestMode: Boolean,
    onOpenSaveProgressAuth: () -> Unit,
    onStartTandemWithUser: (userId: String, displayName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with Mascot Barnaby
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_barnaby_1793914916960),
                    contentDescription = "Barnaby Ranking",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🏆 Classement Mondial & Récompenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "⭐ ${profile.xp} XP • 🔥 Série de ${profile.streakDays} jour(s) • Niveau ${profile.cefrLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Guest Mode Invitation Card for Cloud Leaderboard & Social Features
        if (isGuestMode) {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "👤 Vous êtes en Mode Invité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "🎉 Créez ou connectez votre compte pour sauvegarder vos ${profile.xp} XP ⭐ dans le cloud, apparaître dans le Classement Mondial, ajouter des amis et participer aux défis communautaires !",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onOpenSaveProgressAuth,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ranking_guest_sign_in_button")
                    ) {
                        Text("💾 Sauvegarder ma progression & Rejoindre le Classement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Daily & Weekly Quests (🎁 Rewards & 🎯 Goals)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🎁 Défis Quotidiens & Hebdomadaires",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                QuestRowItem(
                    emoji = "🎯",
                    title = "Défi du Jour : Gagner 100 XP",
                    progressText = "${profile.xp.coerceAtMost(100L)} / 100 ⭐ XP",
                    completed = profile.xp >= 100L
                )
                QuestRowItem(
                    emoji = "🔥",
                    title = "Défi Série : Maintenir 3 jours de Streak",
                    progressText = "${profile.streakDays.coerceAtMost(3L)} / 3 🔥",
                    completed = profile.streakDays >= 3L
                )
                QuestRowItem(
                    emoji = "📚",
                    title = "Mission Spéciale : Terminer 3 leçons ou unités",
                    progressText = "${profile.lessonsCompleted.coerceAtMost(3L)} / 3 📚",
                    completed = profile.lessonsCompleted >= 3L
                )
            }
        }

        // Badges & Achievements (🏆 Achievements)
        Text(
            text = "🏆 Badges & Succès Débloqués",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EnglishCurriculum.badges.forEach { badge ->
                val unlocked = profile.xp >= badge.requiredXp
                Surface(
                    color = if (unlocked) SunnyYellow.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${if (unlocked) "✅" else "🔒"} ${badge.titleFr}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${badge.descriptionFr} (Objectif : ⭐ ${badge.requiredXp} XP)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (unlocked) {
                            Text("🎉 Débloqué", color = Color(0xFFB45309), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // Global Cloud Leaderboard
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = VibrantOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🌍 Classement Mondial Temps Réel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val entries = if (leaderboard.isNotEmpty()) {
                    leaderboard
                } else {
                    listOf(
                        LeaderboardEntry(
                            userId = profile.userId,
                            displayName = "${profile.displayName} (Vous)",
                            cefrLevel = profile.cefrLevel,
                            xp = profile.xp,
                            streakDays = profile.streakDays,
                            badgesCount = 2L
                        ),
                        LeaderboardEntry(
                            userId = "mentor_sophie",
                            displayName = "Sophie M. 🌟",
                            cefrLevel = "B2",
                            xp = 420L,
                            streakDays = 7L,
                            badgesCount = 3L
                        ),
                        LeaderboardEntry(
                            userId = "mentor_karim",
                            displayName = "Karim B. 🚀",
                            cefrLevel = "C1",
                            xp = 680L,
                            streakDays = 12L,
                            badgesCount = 4L
                        )
                    ).sortedByDescending { it.xp }
                }

                entries.forEachIndexed { idx, entry ->
                    val medal = when (idx) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "#${idx + 1}"
                    }
                    Surface(
                        color = if (entry.userId == profile.userId) {
                            FeatherGreen.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$medal  ${entry.displayName} (${entry.cefrLevel})",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "⭐ ${entry.xp} XP • 🔥 ${entry.streakDays}j Streak",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!isGuestMode && entry.userId.isNotBlank() && entry.userId != profile.userId && !entry.userId.startsWith("mentor_")) {
                                OutlinedButton(
                                    onClick = { onStartTandemWithUser(entry.userId, entry.displayName) },
                                    modifier = Modifier.testTag("leaderboard_tandem_${entry.userId}")
                                ) {
                                    Text("💬 Tandem")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestRowItem(
    emoji: String,
    title: String,
    progressText: String,
    completed: Boolean
) {
    Surface(
        color = if (completed) FeatherGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = if (completed) "✅ Complété" else progressText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (completed) FeatherGreen else MaterialTheme.colorScheme.primary
            )
        }
    }
}
