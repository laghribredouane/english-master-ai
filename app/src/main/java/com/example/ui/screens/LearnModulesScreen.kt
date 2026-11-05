package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.WritingAssessmentResult
import com.example.data.curriculum.EnglishCurriculum
import com.example.data.model.CatalogItem
import com.example.data.model.UserFlashcard
import com.example.util.PronunciationAnalysis
import com.example.util.PronunciationEvaluator
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnModulesTabScreen(
    selectedModuleKey: String,
    onSelectModuleKey: (String) -> Unit,
    flashcards: List<UserFlashcard>,
    customCatalog: List<CatalogItem>,
    isAiLoading: Boolean,
    writingResult: WritingAssessmentResult?,
    onSpeakText: (String, Boolean, Float) -> Unit,
    onAddFlashcard: (word: String, fr: String, ar: String, phonetic: String, example: String, category: String, cefr: String) -> Unit,
    onReviewFlashcard: (UserFlashcard, Boolean) -> Unit,
    onRecordModuleScore: (moduleType: String, itemId: String, title: String, cefr: String, score: Int, xp: Int, feedback: String) -> Unit,
    onEvaluateWriting: (text: String, promptTitle: String, cefr: String) -> Unit
) {
    val modules = listOf(
        "vocabulary" to "A. Vocab & SRS",
        "grammar" to "B. Grammar",
        "listening" to "C. Listening",
        "speaking" to "D. Speaking",
        "reading" to "E. Reading",
        "writing" to "F. Writing IA"
    )
    val selectedTabIndex = modules.indexOfFirst { it.first == selectedModuleKey }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 12.dp
        ) {
            modules.forEachIndexed { index, (key, title) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onSelectModuleKey(key) },
                    text = { Text(title) },
                    modifier = Modifier.testTag("module_tab_$key")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (modules[selectedTabIndex].first) {
                "vocabulary" -> VocabularyAndSrsSection(
                    flashcards = flashcards,
                    customCatalog = customCatalog.filter { it.itemType == "vocabulary" },
                    onSpeakText = { text -> onSpeakText(text, true, 1.0f) },
                    onAddFlashcard = onAddFlashcard,
                    onReviewFlashcard = onReviewFlashcard
                )
                "grammar" -> GrammarSection(
                    onSpeakText = { text -> onSpeakText(text, true, 1.0f) },
                    onRecordModuleScore = onRecordModuleScore
                )
                "listening" -> ListeningSection(
                    onSpeakWithSpeed = { text, rate -> onSpeakText(text, true, rate) },
                    onRecordModuleScore = onRecordModuleScore
                )
                "speaking" -> SpeakingSection(
                    onSpeakWithAccent = { text, isUk -> onSpeakText(text, isUk, 1.0f) },
                    onRecordModuleScore = onRecordModuleScore
                )
                "reading" -> ReadingSection(
                    onSpeakText = { text -> onSpeakText(text, true, 1.0f) },
                    onRecordModuleScore = onRecordModuleScore
                )
                "writing" -> WritingSection(
                    isAiLoading = isAiLoading,
                    writingResult = writingResult,
                    onEvaluateWriting = onEvaluateWriting
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VocabularyAndSrsSection(
    flashcards: List<UserFlashcard>,
    customCatalog: List<CatalogItem>,
    onSpeakText: (String) -> Unit,
    onAddFlashcard: (String, String, String, String, String, String, String) -> Unit,
    onReviewFlashcard: (UserFlashcard, Boolean) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var currentCardIdx by remember { mutableIntStateOf(0) }
    var cardFlipped by remember { mutableStateOf(false) }

    Text("Révision Intelligente (Répétition Espacée Leitner SRS)", style = MaterialTheme.typography.titleLarge)

    if (flashcards.isNotEmpty()) {
        val activeCard = flashcards[currentCardIdx % flashcards.size]
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(onClick = {}, label = { Text("Boîte SRS ${activeCard.boxLevel}/5 • ${activeCard.cefrLevel}") })
                    AssistChip(onClick = {}, label = { Text(activeCard.category) })
                }

                Text(
                    text = if (!cardFlipped) activeCard.word else "${activeCard.translationFr}\n${activeCard.translationAr}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeCard.phonetic,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (cardFlipped) {
                    Text(
                        text = "Ex: \"${activeCard.example}\"",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onSpeakText("${activeCard.word}. ${activeCard.example}") },
                        modifier = Modifier.testTag("srs_speak_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audio")
                    }
                    OutlinedButton(
                        onClick = { cardFlipped = !cardFlipped },
                        modifier = Modifier.testTag("srs_flip_button")
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (cardFlipped) "Voir l'anglais" else "Retourner")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onReviewFlashcard(activeCard, false)
                            cardFlipped = false
                            currentCardIdx++
                        },
                        modifier = Modifier.weight(1f).testTag("srs_hard_button")
                    ) {
                        Text("À revoir (Boîte 1)")
                    }
                    Button(
                        onClick = {
                            onReviewFlashcard(activeCard, true)
                            cardFlipped = false
                            currentCardIdx++
                        },
                        modifier = Modifier.weight(1f).testTag("srs_easy_button")
                    ) {
                        Text("Je maîtrise (+20 XP)")
                    }
                }
            }
        }
    }

    Text("Dictionnaire Thématique, Idiomes & Phrasal Verbs", style = MaterialTheme.typography.titleLarge)

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Daily", "Business", "Idiom", "Phrasal Verb", "Academic").forEach { cat ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat) }
            )
        }
    }

    val filteredVocab = EnglishCurriculum.vocabularyCatalog.filter {
        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    filteredVocab.forEach { item ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.word}  ${item.phonetic}", style = MaterialTheme.typography.titleLarge)
                        Text("${item.category} • Niveau ${item.cefrLevel}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = { onSpeakText("${item.word}. ${item.exampleEn}") }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio")
                    }
                }

                Text("🇫🇷 ${item.translationFr}", fontWeight = FontWeight.Bold)
                Text("🇸🇦 ${item.translationAr}", fontWeight = FontWeight.Bold)
                Text("💬 ${item.exampleEn}", style = MaterialTheme.typography.bodyMedium)
                Text("→ ${item.exampleFr}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Synonymes : ${item.synonyms.joinToString(", ")}  |  Antonymes : ${item.antonyms.joinToString(", ")}",
                    style = MaterialTheme.typography.labelLarge
                )

                Button(
                    onClick = {
                        onAddFlashcard(
                            item.word,
                            item.translationFr,
                            item.translationAr,
                            item.phonetic,
                            item.exampleEn,
                            item.category,
                            item.cefrLevel
                        )
                    },
                    modifier = Modifier.align(Alignment.End).testTag("add_flashcard_${item.id}")
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajouter aux Flashcards & Favoris (+15 XP)")
                }
            }
        }
    }

    if (customCatalog.isNotEmpty()) {
        Text("Vocabulaire Publié par l'Administrateur", style = MaterialTheme.typography.titleMedium)
        customCatalog.forEach { custom ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${custom.title} (${custom.cefrLevel})", fontWeight = FontWeight.Bold)
                    Text("🇫🇷 ${custom.translationFr} | 🇸🇦 ${custom.translationAr}")
                    Text(custom.contentBody, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun GrammarSection(
    onSpeakText: (String) -> Unit,
    onRecordModuleScore: (String, String, String, String, Int, Int, String) -> Unit
) {
    Text("Tous les Temps Anglais & Règles Grammaticales (A1–C2)", style = MaterialTheme.typography.titleLarge)

    EnglishCurriculum.grammarCatalog.forEach { topic ->
        var selectedAnswer by remember(topic.id) { mutableStateOf<String?>(null) }
        var validated by remember(topic.id) { mutableStateOf(false) }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(topic.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    AssistChip(onClick = {}, label = { Text(topic.cefrLevel) })
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Formule : ${topic.formula}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text("🇫🇷 ${topic.summaryFr}", style = MaterialTheme.typography.bodyMedium)
                Text("🇸🇦 ${topic.summaryAr}", style = MaterialTheme.typography.bodyMedium)
                Text("🇬🇧 ${topic.summaryEn}", style = MaterialTheme.typography.bodyMedium)

                topic.examples.forEach { ex ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• $ex", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { onSpeakText(ex) }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Exercice d'application :", fontWeight = FontWeight.Bold)
                Text(topic.exerciseQuestion, style = MaterialTheme.typography.bodyLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topic.exerciseOptions.forEach { opt ->
                        FilterChip(
                            selected = selectedAnswer == opt,
                            onClick = { if (!validated) selectedAnswer = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                if (!validated && selectedAnswer != null) {
                    Button(
                        onClick = {
                            validated = true
                            val isCorrect = selectedAnswer == topic.correctOption
                            onRecordModuleScore(
                                "grammar",
                                topic.id,
                                topic.title,
                                topic.cefrLevel,
                                if (isCorrect) 100 else 50,
                                if (isCorrect) 35 else 10,
                                topic.explanationFr
                            )
                        }
                    ) {
                        Text("Vérifier & Voir l'explication")
                    }
                }

                if (validated) {
                    val isCorrect = selectedAnswer == topic.correctOption
                    Surface(
                        color = if (isCorrect) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isCorrect) "✅ Correct ! (+35 XP)" else "❌ Réponse attendue : ${topic.correctOption}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(topic.explanationFr, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListeningSection(
    onSpeakWithSpeed: (String, Float) -> Unit,
    onRecordModuleScore: (String, String, String, String, Int, Int, String) -> Unit
) {
    var playbackRate by remember { mutableFloatStateOf(1.0f) }
    var showTranslations by remember { mutableStateOf(true) }

    Text("Compréhension Orale, Podcasts & Dictées Interactives", style = MaterialTheme.typography.titleLarge)

    // Speed control bar
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Speed, contentDescription = null)
            Text("Vitesse Audio :", fontWeight = FontWeight.Bold)
            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                FilterChip(
                    selected = playbackRate == speed,
                    onClick = { playbackRate = speed },
                    label = { Text("${speed}x") }
                )
            }
        }
    }

    FilterChip(
        selected = showTranslations,
        onClick = { showTranslations = !showTranslations },
        label = { Text(if (showTranslations) "Masquer sous-titres FR/AR" else "Afficher sous-titres interactifs FR/AR") }
    )

    EnglishCurriculum.listeningCatalog.forEach { dialogue ->
        var dictationInput by remember(dialogue.id) { mutableStateOf("") }
        var dictationChecked by remember(dialogue.id) { mutableStateOf(false) }
        var selectedQuizOpt by remember(dialogue.id) { mutableStateOf<String?>(null) }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(dialogue.title, style = MaterialTheme.typography.titleLarge)
                        Text("${dialogue.category} • Niveau ${dialogue.cefrLevel}", color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            val fullText = dialogue.transcriptLines.joinToString(" ") { it.english }
                            onSpeakWithSpeed(fullText, playbackRate)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Écouter (${playbackRate}x)")
                    }
                }

                dialogue.transcriptLines.forEach { line ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${line.speaker}: ${line.english}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                OutlinedButton(onClick = { onSpeakWithSpeed(line.english, playbackRate) }) {
                                    Text("🔊")
                                }
                            }
                            if (showTranslations) {
                                Text("🇫🇷 ${line.french}", style = MaterialTheme.typography.bodyMedium)
                                Text("🇸🇦 ${line.arabic}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Interactive Dictation
                Text("Dictée Interactive :", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onSpeakWithSpeed(dialogue.dictationTarget, playbackRate) }) {
                        Text("🔊 Écouter la phrase à dicter")
                    }
                }
                OutlinedTextField(
                    value = dictationInput,
                    onValueChange = { dictationInput = it },
                    label = { Text("Tapez ce que vous entendez en anglais") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        dictationChecked = true
                        val analysis = PronunciationEvaluator.evaluateSpokenAttempt(dialogue.dictationTarget, dictationInput)
                        onRecordModuleScore(
                            "listening",
                            dialogue.id,
                            dialogue.title,
                            dialogue.cefrLevel,
                            analysis.fluencyScore,
                            35,
                            "Dictation score: ${analysis.fluencyScore}%"
                        )
                    }
                ) {
                    Text("Vérifier la dictée & la compréhension (+35 XP)")
                }
                if (dictationChecked) {
                    Text("Phrase exacte : \"${dialogue.dictationTarget}\"", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Text("Question de compréhension : ${dialogue.comprehensionQuestion}", fontWeight = FontWeight.Bold)
                dialogue.comprehensionOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedQuizOpt == opt,
                        onClick = { selectedQuizOpt = opt },
                        label = { Text(opt) }
                    )
                }
                if (selectedQuizOpt != null) {
                    Text(
                        text = if (selectedQuizOpt == dialogue.correctOption) "✅ Exact !" else "❌ Réponse : ${dialogue.correctOption}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeakingSection(
    onSpeakWithAccent: (String, Boolean) -> Unit,
    onRecordModuleScore: (String, String, String, String, Int, Int, String) -> Unit
) {
    var useUkAccent by remember { mutableStateOf(true) }
    var activeTargetSentence by remember { mutableStateOf(EnglishCurriculum.speakingDrills.first().targetSentence) }
    var spokenTranscript by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<PronunciationAnalysis?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = matches?.firstOrNull().orEmpty()
            if (recognized.isNotBlank()) {
                spokenTranscript = recognized
                val eval = PronunciationEvaluator.evaluateSpokenAttempt(activeTargetSentence, recognized)
                analysisResult = eval
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (useUkAccent) "en-GB" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, activeTargetSentence)
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                spokenTranscript = activeTargetSentence
                analysisResult = PronunciationEvaluator.evaluateSpokenAttempt(activeTargetSentence, activeTargetSentence)
            }
        }
    }

    Text("Expression Orale, Accents UK/US & Analyse de Prononciation", style = MaterialTheme.typography.titleLarge)

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = useUkAccent,
            onClick = { useUkAccent = true },
            label = { Text("🇬🇧 Accent Britannique (UK)") }
        )
        FilterChip(
            selected = !useUkAccent,
            onClick = { useUkAccent = false },
            label = { Text("🇺🇸 Accent Américain (US)") }
        )
    }

    EnglishCurriculum.speakingDrills.forEach { drill ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(drill.title, style = MaterialTheme.typography.titleLarge)
                    AssistChip(onClick = {}, label = { Text(drill.cefrLevel) })
                }
                Text("Situation : ${drill.situation}", style = MaterialTheme.typography.bodyMedium)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${drill.targetSentence}\"",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text("🇫🇷 ${drill.translationFr}", style = MaterialTheme.typography.bodyMedium)
                Text("🇸🇦 ${drill.translationAr}", style = MaterialTheme.typography.bodyMedium)
                Text("💡 Conseil UK/US : ${drill.tipUkUs}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onSpeakWithAccent(drill.targetSentence, useUkAccent) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (useUkAccent) "Modèle UK 🇬🇧" else "Modèle US 🇺🇸")
                    }
                    Button(
                        onClick = {
                            activeTargetSentence = drill.targetSentence
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parler au Micro")
                    }
                }
            }
        }
    }

    // Oral Evaluation Box (supports voice recognition OR manual phonetic test input on headless emulators)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Analyseur de Fluidité & Prononciation", style = MaterialTheme.typography.titleMedium)
            Text("Phrase cible active : \"$activeTargetSentence\"", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = spokenTranscript,
                onValueChange = { spokenTranscript = it },
                label = { Text("Transcription vocale détectée (ou saisie test)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val eval = PronunciationEvaluator.evaluateSpokenAttempt(activeTargetSentence, spokenTranscript)
                    analysisResult = eval
                    onRecordModuleScore(
                        "speaking",
                        "spk_eval",
                        "Speaking Pronunciation Practice",
                        "B2",
                        eval.fluencyScore,
                        40,
                        eval.feedbackFr
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("evaluate_pronunciation_button")
            ) {
                Text("Analyser ma Prononciation & mon Score de Fluidité (+40 XP)")
            }

            if (analysisResult != null) {
                val res = analysisResult!!
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Score de Fluidité : ${res.fluencyScore} / 100", style = MaterialTheme.typography.titleLarge)
                        Text(res.feedbackFr)
                        if (res.matchedWords.isNotEmpty()) {
                            Text("✅ Mots validés : ${res.matchedWords.joinToString(", ")}")
                        }
                        if (res.missedWords.isNotEmpty()) {
                            Text("🎯 Mots à retravailler : ${res.missedWords.joinToString(", ")}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingSection(
    onSpeakText: (String) -> Unit,
    onRecordModuleScore: (String, String, String, String, Int, Int, String) -> Unit
) {
    Text("Lecture, Textes Professionnels & Glossaire Interactif", style = MaterialTheme.typography.titleLarge)

    EnglishCurriculum.readingArticles.forEach { article ->
        var selectedWordMeaning by remember(article.id) { mutableStateOf<Pair<String, String>?>(null) }
        var selectedOption by remember(article.id) { mutableStateOf<String?>(null) }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(article.title, style = MaterialTheme.typography.titleLarge)
                        Text("${article.genre} • Niveau ${article.cefrLevel}", color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = { onSpeakText(article.paragraphs.joinToString(" ")) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    }
                }

                article.paragraphs.forEach { p ->
                    Text(p, style = MaterialTheme.typography.bodyLarge)
                }

                Text("Glossaire Interactif (Touchez un mot pour sa traduction FR/AR) :", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    article.glossary.forEach { (word, meaning) ->
                        AssistChip(
                            onClick = { selectedWordMeaning = word to meaning },
                            label = { Text(word) }
                        )
                    }
                }

                if (selectedWordMeaning != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📖 ${selectedWordMeaning!!.first} : ${selectedWordMeaning!!.second}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text("Question de compréhension : ${article.question}", fontWeight = FontWeight.Bold)
                article.options.forEach { opt ->
                    FilterChip(
                        selected = selectedOption == opt,
                        onClick = {
                            selectedOption = opt
                            val ok = opt == article.correctAnswer
                            onRecordModuleScore(
                                "reading",
                                article.id,
                                article.title,
                                article.cefrLevel,
                                if (ok) 100 else 50,
                                if (ok) 35 else 10,
                                article.explanation
                            )
                        },
                        label = { Text(opt) }
                    )
                }

                if (selectedOption != null) {
                    val ok = selectedOption == article.correctAnswer
                    Text(
                        text = if (ok) "✅ Correct ! ${article.explanation}" else "❌ Réponse : ${article.correctAnswer}. ${article.explanation}",
                        color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WritingSection(
    isAiLoading: Boolean,
    writingResult: WritingAssessmentResult?,
    onEvaluateWriting: (String, String, String) -> Unit
) {
    var selectedPrompt by remember { mutableStateOf(EnglishCurriculum.writingPrompts.first()) }
    var draftText by remember(selectedPrompt.id) { mutableStateOf(selectedPrompt.starterTemplate) }

    Text("Expression Écrite & Correction par IA (Gemini 3.8)", style = MaterialTheme.typography.titleLarge)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EnglishCurriculum.writingPrompts.forEach { prompt ->
            FilterChip(
                selected = selectedPrompt.id == prompt.id,
                onClick = { selectedPrompt = prompt },
                label = { Text("${prompt.category} (${prompt.cefrLevel})") }
            )
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(selectedPrompt.title, style = MaterialTheme.typography.titleLarge)
            Text(selectedPrompt.instructionsFr, style = MaterialTheme.typography.bodyMedium)
            Text("Mots-clés recommandés : ${selectedPrompt.targetKeywords.joinToString(", ")}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = draftText,
                onValueChange = { draftText = it },
                label = { Text("Votre rédaction en anglais") },
                minLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("writing_input_field")
            )

            Button(
                onClick = {
                    onEvaluateWriting(draftText, selectedPrompt.title, selectedPrompt.cefrLevel)
                },
                enabled = !isAiLoading && draftText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("writing_evaluate_button")
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyse grammaticale et stylistique par l'IA...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Corriger & Évaluer avec l'IA (+45 XP)")
                }
            }
        }
    }

    if (writingResult != null) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Score Qualité IA : ${writingResult.score}/100", style = MaterialTheme.typography.titleLarge)
                    AssistChip(onClick = {}, label = { Text("Niveau estimé : ${writingResult.cefrEstimate}") })
                }
                Text("Version corrigée et améliorée :", fontWeight = FontWeight.Bold)
                Text(writingResult.correctedText, style = MaterialTheme.typography.bodyLarge)
                Text("Explications grammaticales & orthographiques :", fontWeight = FontWeight.Bold)
                Text(writingResult.grammarAndSpellingFeedback, style = MaterialTheme.typography.bodyMedium)
                if (writingResult.styleAndVocabularyUpgrades.isNotEmpty()) {
                    Text("Suggestions de vocabulaire avancé :", fontWeight = FontWeight.Bold)
                    writingResult.styleAndVocabularyUpgrades.forEach { up ->
                        Text("• $up", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
