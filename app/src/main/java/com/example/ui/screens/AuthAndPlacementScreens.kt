package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.curriculum.EnglishCurriculum
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun performSignOut(
    context: Context,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    selectedLang: String,
    onChangeLang: (String) -> Unit,
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: (() -> Unit)? = null
) {
    BackHandler(enabled = onContinueAsGuest != null) {
        onContinueAsGuest?.invoke()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Language selector row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
            listOf("fr" to "🌍 Français", "en" to "🌍 English", "ar" to "🌍 العربية").forEach { (code, label) ->
                FilterChip(
                    selected = selectedLang == code,
                    onClick = { onChangeLang(code) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("auth_lang_$code")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mascot & Gamified Hero Card
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_barnaby_1793914916960),
                    contentDescription = "Barnaby Mascot English Master AI",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "🎯 CEFR A1 → C2 • 🤖 AI TUTOR 24/7 • 🎮 GAMIFIED",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "English Master AI",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = when (selectedLang) {
                        "ar" -> "احفظ تقدمك ومفرداتك وسلسلة أيامك 🔥 عبر جميع أجهزتك"
                        "en" -> "Save your XP ⭐, Streak 🔥, and lessons across all your devices!"
                        else -> "Sauvegardez vos XP ⭐, votre série 🔥 et vos leçons dans le cloud sans aucune perte !"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cloud Account & Guest Migration Highlights
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔐 Mode Connecté vs 👤 Mode Invité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "• 💾 Transfert et fusion automatiques de toute votre progression Invité vers votre compte Cloud\n" +
                        "• 🏆 Accès au Classement Mondial, défis communautaires et conversations Tandem 1-to-1\n" +
                        "• 🤖 Sauvegarde complète de vos échanges avec Prof. Victoria sur tous vos appareils",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🔐 Continuer avec Google (Sauvegarde Cloud)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (onContinueAsGuest != null) {
                    OutlinedButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_as_guest_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "👤 Continuer en Mode Invité (Sans compte)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlacementTestScreen(
    onSpeakText: (String) -> Unit,
    onFinishPlacement: (correctCount: Int, totalCount: Int, recommendedCefr: String, recommendedTrackId: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val questions = EnglishCurriculum.placementQuestions
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var showExplanation by remember { mutableStateOf(false) }
    val answersByQuestion = remember { mutableStateListOf<Boolean>() }
    var testCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Test de Niveau Initial (A1 → C2)",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Grammaire • Vocabulaire • Écoute • Lecture • Syntaxe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_placement_button")) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        if (!testCompleted && currentQuestion != null) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Niveau évalué : ${currentQuestion.cefrLevel}") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(currentQuestion.skillCategory) }
                        )
                    }

                    Text(
                        text = "Question ${currentIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = currentQuestion.prompt,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (currentQuestion.audioTranscript != null) {
                        OutlinedButton(
                            onClick = { onSpeakText(currentQuestion.audioTranscript) },
                            modifier = Modifier.testTag("placement_play_audio")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play audio")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Écouter l'extrait audio (TTS)")
                        }
                    }

                    currentQuestion.options.forEachIndexed { idx, option ->
                        Card(
                            onClick = {
                                if (!showExplanation) {
                                    selectedOption = idx
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    showExplanation && idx == currentQuestion.correctIndex ->
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    selectedOption == idx ->
                                        MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("placement_option_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == idx,
                                    onClick = {
                                        if (!showExplanation) selectedOption = idx
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    if (showExplanation) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (selectedOption == currentQuestion.correctIndex)
                                        "✅ Excellente réponse !"
                                    else
                                        "❌ Bonne réponse : ${currentQuestion.options[currentQuestion.correctIndex]}",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = currentQuestion.explanationFr, style = MaterialTheme.typography.bodyMedium)
                                Text(text = currentQuestion.explanationAr, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!showExplanation) {
                            Button(
                                onClick = {
                                    if (selectedOption != -1) {
                                        answersByQuestion.add(selectedOption == currentQuestion.correctIndex)
                                        showExplanation = true
                                    }
                                },
                                enabled = selectedOption != -1,
                                modifier = Modifier.testTag("placement_validate_button")
                            ) {
                                Text("Valider la réponse")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < questions.size) {
                                        currentIndex++
                                        selectedOption = -1
                                        showExplanation = false
                                    } else {
                                        testCompleted = true
                                    }
                                },
                                modifier = Modifier.testTag("placement_next_button")
                            ) {
                                Text(if (currentIndex + 1 < questions.size) "Question suivante" else "Voir le rapport détaillé")
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed Placement Report
            val correctCount = answersByQuestion.count { it }
            val recommendedCefr = when (correctCount) {
                0, 1 -> "A1"
                2 -> "A2"
                3 -> "B1"
                4 -> "B2"
                5 -> "C1"
                else -> "C2"
            }
            val recommendedTrack = when (recommendedCefr) {
                "A1", "A2" -> "track_general"
                "B1" -> "track_conversational"
                "B2" -> "track_professional"
                "C1" -> "track_ielts"
                else -> "track_business"
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recommendedCefr,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Rapport Pédagogique Complet",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Score : $correctCount / ${questions.size} • Niveau CECRL attribué : $recommendedCefr",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    questions.forEachIndexed { index, q ->
                        val ok = answersByQuestion.getOrNull(index) == true
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${q.skillCategory} (${q.cefrLevel})", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (ok) "Maîtrisé ✓" else "À renforcer",
                                color = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onFinishPlacement(correctCount, questions.size, recommendedCefr, recommendedTrack)
                            onClose()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("placement_save_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activer mon parcours personnalisé ($recommendedCefr) +120 XP")
                    }
                }
            }
        }
    }
}
