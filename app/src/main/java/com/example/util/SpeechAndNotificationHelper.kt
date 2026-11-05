package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import java.util.Locale

data class PronunciationAnalysis(
    val fluencyScore: Int,
    val matchedWords: List<String>,
    val missedWords: List<String>,
    val feedbackFr: String,
    val feedbackEn: String
)

class SpeechController(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var currentAccent: Locale = Locale.UK
    private var speechRate: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                tts?.language = currentAccent
            }
        }
    }

    fun setAccent(useUkAccent: Boolean) {
        currentAccent = if (useUkAccent) Locale.UK else Locale.US
        if (isReady) {
            tts?.language = currentAccent
        }
    }

    fun setSpeed(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        if (isReady) {
            tts?.setSpeechRate(speechRate)
        }
    }

    fun speak(text: String) {
        if (isReady && text.isNotBlank()) {
            tts?.language = currentAccent
            tts?.setSpeechRate(speechRate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "em_tts_${System.currentTimeMillis()}")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

object PronunciationEvaluator {
    fun evaluateSpokenAttempt(targetSentence: String, spokenText: String): PronunciationAnalysis {
        val cleanTarget = targetSentence.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val cleanSpoken = spokenText.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        if (cleanTarget.isEmpty() || cleanSpoken.isEmpty()) {
            return PronunciationAnalysis(
                fluencyScore = 0,
                matchedWords = emptyList(),
                missedWords = cleanTarget,
                feedbackFr = "Aucun mot détecté. Écoutez le modèle audio puis réessayez.",
                feedbackEn = "No words detected. Listen to the audio model and try again."
            )
        }

        val matched = cleanTarget.filter { it in cleanSpoken }
        val missed = cleanTarget.filter { it !in cleanSpoken }
        val ratio = matched.size.toFloat() / cleanTarget.size.toFloat()
        val score = (ratio * 100).toInt().coerceIn(10, 100)

        val feedbackFr = when {
            score >= 90 -> "Excellente fluidité et prononciation ! Tous les mots clés sont bien articulés."
            score >= 70 -> "Très bon essai ($score/100). Travaillez l'articulation de : ${missed.take(3).joinToString(", ")}."
            else -> "Continuez à pratiquer ($score/100). Concentrez-vous sur : ${missed.take(4).joinToString(", ")}."
        }
        val feedbackEn = when {
            score >= 90 -> "Outstanding fluency and articulation! Every key stress point was clear."
            score >= 70 -> "Strong effort ($score/100). Focus on pronouncing: ${missed.take(3).joinToString(", ")}."
            else -> "Keep practicing ($score/100). Pay special attention to: ${missed.take(4).joinToString(", ")}."
        }

        return PronunciationAnalysis(
            fluencyScore = score,
            matchedWords = matched,
            missedWords = missed,
            feedbackFr = feedbackFr,
            feedbackEn = feedbackEn
        )
    }
}

object StudyNotificationHelper {
    private const val CHANNEL_ID = "english_master_reminders"

    fun sendStudyReminderNotification(context: Context, title: String, message: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "English Master Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels quotidiens, révisions SRS et nouveaux défis"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
        return true
    }
}
