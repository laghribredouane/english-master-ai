package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiTutorService"

data class TutorStructuredReply(
    val englishResponse: String,
    val grammarCorrection: String,
    val pedagogicalExplanation: String,
    val suggestedFollowUp: String,
    val fluencyTip: String
)

data class WritingAssessmentResult(
    val score: Int,
    val cefrEstimate: String,
    val correctedText: String,
    val grammarAndSpellingFeedback: String,
    val styleAndVocabularyUpgrades: List<String>
)

data class GeneratedQuizItem(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

class AiTutorService {

    private val tutorReplySchema = Schema.obj(
        properties = mapOf(
            "englishResponse" to Schema.string(description = "Natural, encouraging reply in English adapted to the learner's CEFR level and scenario."),
            "grammarCorrection" to Schema.string(description = "Corrected version of the learner's message if it had mistakes, or 'Perfect! No errors detected.' if flawless."),
            "pedagogicalExplanation" to Schema.string(description = "Clear explanation of grammar/vocabulary rules in the user's preferred explanation language (French, Arabic, or English)."),
            "suggestedFollowUp" to Schema.string(description = "A helpful example sentence the learner can say next in English."),
            "fluencyTip" to Schema.string(description = "Short pronunciation or idiomatic tip.")
        )
    )

    private val writingAssessmentSchema = Schema.obj(
        properties = mapOf(
            "score" to Schema.integer(description = "Writing quality score from 0 to 100"),
            "cefrEstimate" to Schema.string(description = "Estimated CEFR level of the text (A1, A2, B1, B2, C1, or C2)"),
            "correctedText" to Schema.string(description = "Polished, grammatically correct version of the user's text"),
            "grammarAndSpellingFeedback" to Schema.string(description = "Detailed grammar and spelling explanation in the user's UI language"),
            "styleAndVocabularyUpgrades" to Schema.array(
                items = Schema.string(),
                description = "3 specific C1/B2 vocabulary or style improvements"
            )
        )
    )

    private val dynamicQuizSchema = Schema.obj(
        properties = mapOf(
            "question" to Schema.string(description = "English exercise question adapted to the requested CEFR level and topic"),
            "options" to Schema.array(items = Schema.string(), description = "Exactly 4 answer choices"),
            "correctAnswer" to Schema.string(description = "The exact correct option matching one of the 4 choices"),
            "explanation" to Schema.string(description = "Pedagogical explanation of why the answer is correct")
        )
    )

    private val tutorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = tutorReplySchema
            }
        )
    }

    private val writingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = writingAssessmentSchema
            }
        )
    }

    private val quizGeneratorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = dynamicQuizSchema
            }
        )
    }

    suspend fun askTutor(
        userMessage: String,
        cefrLevel: String,
        scenario: String,
        uiLanguage: String,
        recentHistory: List<Pair<String, String>>
    ): Result<TutorStructuredReply> {
        val langName = when (uiLanguage) {
            "ar" -> "Arabic (العربية)"
            "en" -> "English"
            else -> "French (Français)"
        }
        val historyContext = recentHistory.takeLast(6).joinToString("\n") { (role, msg) ->
            "${role.uppercase()}: $msg"
        }
        val prompt = """
            You are Professor Victoria, an expert AI English Tutor in the 'English Master AI' app.
            Learner CEFR Level: $cefrLevel
            Conversation Scenario: $scenario (e.g. free conversation, job_interview, business, travel, daily, grammar_coach)
            Preferred language for 'pedagogicalExplanation': $langName
            
            Recent conversation context:
            $historyContext
            
            Learner's latest message:
            "$userMessage"
            
            Respond as a warm, rigorous private English tutor:
            1. Reply naturally in English ('englishResponse') tailored to CEFR level $cefrLevel and scenario '$scenario', asking an engaging follow-up question.
            2. Provide an instant correction of any grammar, spelling, or phrasing mistake in the learner's message ('grammarCorrection').
            3. Explain the grammar rule or contextual translation in $langName ('pedagogicalExplanation').
            4. Give one natural English response suggestion the learner could say next ('suggestedFollowUp').
            5. Include a short UK/US pronunciation or idiom tip ('fluencyTip').
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                tutorModel.generateContent(prompt)
            }
            val jsonString = response.text ?: throw IllegalStateException("Empty AI response.")
            val json = JSONObject(jsonString)
            Result.success(
                TutorStructuredReply(
                    englishResponse = json.optString("englishResponse", "That's a great point! Could you tell me more?"),
                    grammarCorrection = json.optString("grammarCorrection", "No errors detected."),
                    pedagogicalExplanation = json.optString("pedagogicalExplanation", ""),
                    suggestedFollowUp = json.optString("suggestedFollowUp", ""),
                    fluencyTip = json.optString("fluencyTip", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Tutor timed out", e)
            Result.failure(IllegalStateException("AI Tutor timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Tutor inference failed", e)
            Result.failure(e)
        }
    }

    suspend fun evaluateWriting(
        userText: String,
        promptTitle: String,
        targetCefr: String,
        uiLanguage: String
    ): Result<WritingAssessmentResult> {
        val langName = when (uiLanguage) {
            "ar" -> "Arabic (العربية)"
            "en" -> "English"
            else -> "French (Français)"
        }
        val prompt = """
            Evaluate the following English writing submission for the assignment "$promptTitle" (Target CEFR: $targetCefr).
            Provide explanations in $langName.
            
            Learner Submission:
            "$userText"
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                writingModel.generateContent(prompt)
            }
            val jsonString = response.text ?: throw IllegalStateException("Empty AI writing assessment.")
            val json = JSONObject(jsonString)
            val upgradesArray = json.optJSONArray("styleAndVocabularyUpgrades")
            val upgrades = mutableListOf<String>()
            if (upgradesArray != null) {
                for (i in 0 until upgradesArray.length()) {
                    upgrades.add(upgradesArray.getString(i))
                }
            }
            Result.success(
                WritingAssessmentResult(
                    score = json.optInt("score", 75).coerceIn(0, 100),
                    cefrEstimate = json.optString("cefrEstimate", targetCefr),
                    correctedText = json.optString("correctedText", userText),
                    grammarAndSpellingFeedback = json.optString("grammarAndSpellingFeedback", ""),
                    styleAndVocabularyUpgrades = upgrades
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Writing evaluation timed out", e)
            Result.failure(IllegalStateException("Writing evaluation timed out after 60s."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Writing evaluation failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateCustomExercise(
        topic: String,
        cefrLevel: String,
        uiLanguage: String
    ): Result<GeneratedQuizItem> {
        val langName = when (uiLanguage) {
            "ar" -> "Arabic (العربية)"
            "en" -> "English"
            else -> "French (Français)"
        }
        val prompt = """
            Generate a challenging, high-quality multiple-choice English exercise for CEFR level $cefrLevel on the topic: "$topic".
            Provide 4 distinct options where one is clearly correct, and explain the rule in $langName.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                quizGeneratorModel.generateContent(prompt)
            }
            val jsonString = response.text ?: throw IllegalStateException("Empty quiz generation response.")
            val json = JSONObject(jsonString)
            val arr = json.optJSONArray("options")
            val opts = mutableListOf<String>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    opts.add(arr.getString(i))
                }
            }
            Result.success(
                GeneratedQuizItem(
                    question = json.optString("question", ""),
                    options = opts.ifEmpty { listOf("Option A", "Option B", "Option C", "Option D") },
                    correctAnswer = json.optString("correctAnswer", opts.firstOrNull().orEmpty()),
                    explanation = json.optString("explanation", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Quiz generation timed out", e)
            Result.failure(IllegalStateException("Exercise generation timed out after 60s."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Quiz generation failed", e)
            Result.failure(e)
        }
    }
}
