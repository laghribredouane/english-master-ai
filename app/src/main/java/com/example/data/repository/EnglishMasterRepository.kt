package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CatalogItem
import com.example.data.model.CommunityMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LearningProgress
import com.example.data.model.TutorMessage
import com.example.data.model.UserFlashcard
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EnglishMasterRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun getUserProfileById(userId: String): Result<UserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createInitialProfile(
        displayName: String,
        email: String?,
        cefrLevel: String = "A1",
        uiLanguage: String = "fr",
        dailyGoalMinutes: Long = 15L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "displayName" to displayName.take(80).ifBlank { "Learner" },
            "avatarTag" to "scholar",
            "cefrLevel" to cefrLevel,
            "uiLanguage" to uiLanguage,
            "xp" to 50L,
            "streakDays" to 1L,
            "dailyGoalMinutes" to dailyGoalMinutes,
            "dailyMinutesCompleted" to 5L,
            "wordsLearned" to 0L,
            "lessonsCompleted" to 0L,
            "grammarScore" to 50L,
            "pronunciationScore" to 50L,
            "listeningScore" to 50L,
            "writingScore" to 50L,
            "placementCompleted" to false,
            "subscriptionTier" to "Free",
            "selectedTrackId" to "track_general",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!email.isNullOrBlank()) {
            payload["email"] = email.take(160)
        }
        try {
            docRef.set(payload).await()
            uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updateMap = fields + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updateMap).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 2. Flashcards & Spaced Repetition ---

    fun observeFlashcards(userId: String = requireUserId()): Flow<List<UserFlashcard>> {
        val path = "users/$userId/flashcards"
        return db.collection("users").document(userId).collection("flashcards")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserFlashcard::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createFlashcard(
        word: String,
        translationFr: String,
        translationAr: String,
        phonetic: String,
        example: String,
        category: String,
        cefrLevel: String,
        isFavorite: Boolean = true,
        boxLevel: Long = 1L,
        nextReviewEpochDay: Long = System.currentTimeMillis() / 86_400_000L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cardId = "card_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("flashcards").document(cardId)
        val payload = mapOf(
            "userId" to uid,
            "word" to word.take(120),
            "translationFr" to translationFr.take(200),
            "translationAr" to translationAr.take(200),
            "phonetic" to phonetic.take(120),
            "example" to example.take(500),
            "category" to category.take(60),
            "cefrLevel" to cefrLevel,
            "isFavorite" to isFavorite,
            "boxLevel" to boxLevel.coerceIn(1L, 5L),
            "nextReviewEpochDay" to nextReviewEpochDay.coerceIn(0L, 100000L),
            "mastered" to (boxLevel >= 5L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            cardId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun reviewFlashcard(card: UserFlashcard, remembered: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(card.id.isNotBlank()) { "Card ID required" }
        val docRef = db.collection("users").document(uid).collection("flashcards").document(card.id)
        val newBox = if (remembered) (card.boxLevel + 1L).coerceAtMost(5L) else 1L
        val intervalDays = when (newBox) {
            1L -> 1L
            2L -> 2L
            3L -> 4L
            4L -> 7L
            else -> 14L
        }
        val todayEpoch = (System.currentTimeMillis() / 86_400_000L).coerceAtLeast(1L)
        val nextReview = (todayEpoch + intervalDays).coerceAtMost(100000L)
        val mastered = newBox >= 5L

        try {
            docRef.update(
                mapOf(
                    "boxLevel" to newBox,
                    "nextReviewEpochDay" to nextReview,
                    "mastered" to mastered,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleFlashcardFavorite(cardId: String, isFavorite: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("flashcards").document(cardId)
        try {
            docRef.update(
                mapOf(
                    "isFavorite" to isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 3. Learning Progress ---

    fun observeProgress(userId: String = requireUserId()): Flow<List<LearningProgress>> {
        val path = "users/$userId/progress"
        return db.collection("users").document(userId).collection("progress")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    LearningProgress::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun recordProgress(
        moduleType: String,
        itemId: String,
        title: String,
        cefrLevel: String,
        score: Long,
        maxScore: Long,
        xpEarned: Long,
        feedback: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val recordId = "prog_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("progress").document(recordId)
        val payload = mapOf(
            "userId" to uid,
            "moduleType" to moduleType,
            "itemId" to itemId.take(120),
            "title" to title.take(160),
            "cefrLevel" to cefrLevel,
            "score" to score.coerceIn(0L, 1000L),
            "maxScore" to maxScore.coerceIn(1L, 1000L),
            "xpEarned" to xpEarned.coerceIn(0L, 5000L),
            "feedback" to feedback.take(2000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            recordId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- 4. AI English Tutor Conversation History ---

    fun observeTutorMessages(userId: String = requireUserId()): Flow<List<TutorMessage>> {
        val path = "users/$userId/tutor_messages"
        return db.collection("users").document(userId).collection("tutor_messages")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    TutorMessage::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveTutorMessage(
        role: String,
        scenario: String,
        content: String,
        correction: String = "",
        explanation: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("tutor_messages").document(msgId)
        val payload = mapOf(
            "userId" to uid,
            "role" to role,
            "scenario" to scenario.take(60),
            "content" to content.take(4000).ifBlank { "..." },
            "correction" to correction.take(2000),
            "explanation" to explanation.take(2000),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            msgId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- 5. Global Leaderboard ---

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> {
        val path = "leaderboard"
        return db.collection("leaderboard")
            .whereGreaterThanOrEqualTo("xp", 0L)
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    LeaderboardEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun syncLeaderboardEntry(
        displayName: String,
        cefrLevel: String,
        xp: Long,
        streakDays: Long,
        badgesCount: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("leaderboard").document(uid)
        val payload = mapOf(
            "userId" to uid,
            "displayName" to displayName.take(80).ifBlank { "Learner" },
            "cefrLevel" to cefrLevel,
            "xp" to xp.coerceIn(0L, 10000000L),
            "streakDays" to streakDays.coerceIn(0L, 10000L),
            "badgesCount" to badgesCount.coerceIn(0L, 500L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- 6. Dynamic Admin / Educator Catalog ---

    fun observePublishedCatalog(): Flow<List<CatalogItem>> {
        val path = "custom_catalog"
        return db.collection("custom_catalog")
            .whereEqualTo("published", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CatalogItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createCatalogItem(
        itemType: String,
        title: String,
        subtitle: String,
        cefrLevel: String,
        contentBody: String,
        translationFr: String,
        translationAr: String,
        quizQuestion: String,
        quizOptionsCsv: String,
        quizAnswer: String,
        published: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val itemId = "cat_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("custom_catalog").document(itemId)
        val payload = mapOf(
            "authorId" to uid,
            "itemType" to itemType,
            "title" to title.take(160),
            "subtitle" to subtitle.take(300),
            "cefrLevel" to cefrLevel,
            "contentBody" to contentBody.take(5000),
            "translationFr" to translationFr.take(500),
            "translationAr" to translationAr.take(500),
            "quizQuestion" to quizQuestion.take(500),
            "quizOptionsCsv" to quizOptionsCsv.take(1000),
            "quizAnswer" to quizAnswer.take(300),
            "published" to published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            itemId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCatalogItem(itemId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("custom_catalog").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 7. User-to-User Community & Tandem Conversation Space ---

    fun observeCommunityMessages(roomId: String): Flow<List<CommunityMessage>> {
        val path = "community_messages"
        val cleanRoom = roomId.take(128).ifBlank { "general" }
        return db.collection("community_messages")
            .whereEqualTo("roomId", cleanRoom)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CommunityMessage::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun sendCommunityMessage(
        senderName: String,
        senderCefr: String,
        roomId: String,
        content: String,
        recipientId: String = "",
        recipientName: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgId = "cmsg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("community_messages").document(msgId)
        val validCefr = if (senderCefr in listOf("A1", "A2", "B1", "B2", "C1", "C2")) senderCefr else "A1"
        val payload = mapOf(
            "senderId" to uid,
            "senderName" to senderName.take(80).ifBlank { "Learner" },
            "senderCefr" to validCefr,
            "roomId" to roomId.take(128).ifBlank { "general" },
            "recipientId" to recipientId.take(128),
            "recipientName" to recipientName.take(80),
            "content" to content.trim().take(2000).ifBlank { "Hello!" },
            "correctionSuggestion" to "",
            "correctedByName" to "",
            "likesCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            msgId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun submitPeerCorrection(
        messageId: String,
        correctionSuggestion: String,
        correctedByName: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("community_messages").document(messageId)
        try {
            docRef.update(
                mapOf(
                    "correctionSuggestion" to correctionSuggestion.trim().take(1000),
                    "correctedByName" to correctedByName.trim().take(80).ifBlank { "Peer" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun likeCommunityMessage(
        messageId: String,
        currentLikes: Long
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("community_messages").document(messageId)
        try {
            docRef.update(
                mapOf(
                    "likesCount" to (currentLikes + 1L).coerceIn(0L, 100000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCommunityMessage(messageId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("community_messages").document(messageId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}

