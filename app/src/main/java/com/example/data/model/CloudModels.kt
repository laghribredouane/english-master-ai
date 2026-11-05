package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Learner",
    val email: String? = null,
    val avatarTag: String? = "scholar",
    val cefrLevel: String = "A1",
    val uiLanguage: String = "fr",
    val xp: Long = 0L,
    val streakDays: Long = 1L,
    val dailyGoalMinutes: Long = 15L,
    val dailyMinutesCompleted: Long = 0L,
    val wordsLearned: Long = 0L,
    val lessonsCompleted: Long = 0L,
    val grammarScore: Long = 50L,
    val pronunciationScore: Long = 50L,
    val listeningScore: Long = 50L,
    val writingScore: Long = 50L,
    val placementCompleted: Boolean = false,
    val subscriptionTier: String = "Free",
    val selectedTrackId: String = "track_general",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserFlashcard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val word: String = "",
    val translationFr: String = "",
    val translationAr: String = "",
    val phonetic: String = "",
    val example: String = "",
    val category: String = "Daily",
    val cefrLevel: String = "A1",
    val isFavorite: Boolean = false,
    val boxLevel: Long = 1L,
    val nextReviewEpochDay: Long = 0L,
    val mastered: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LearningProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val moduleType: String = "vocabulary",
    val itemId: String = "",
    val title: String = "",
    val cefrLevel: String = "A1",
    val score: Long = 0L,
    val maxScore: Long = 100L,
    val xpEarned: Long = 10L,
    val feedback: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TutorMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user",
    val scenario: String = "free",
    val content: String = "",
    val correction: String = "",
    val explanation: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val cefrLevel: String = "A1",
    val xp: Long = 0L,
    val streakDays: Long = 1L,
    val badgesCount: Long = 1L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CatalogItem(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val itemType: String = "vocabulary",
    val title: String = "",
    val subtitle: String = "",
    val cefrLevel: String = "B1",
    val contentBody: String = "",
    val translationFr: String = "",
    val translationAr: String = "",
    val quizQuestion: String = "",
    val quizOptionsCsv: String = "",
    val quizAnswer: String = "",
    val published: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "Learner",
    val senderCefr: String = "A1",
    val roomId: String = "general",
    val recipientId: String = "",
    val recipientName: String = "",
    val content: String = "",
    val correctionSuggestion: String = "",
    val correctedByName: String = "",
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

