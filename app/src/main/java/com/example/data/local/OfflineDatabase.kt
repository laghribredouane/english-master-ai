package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_packs")
data class OfflinePackEntity(
    @PrimaryKey val packId: String,
    val title: String,
    val cefrLevel: String,
    val moduleType: String,
    val lessonsCount: Int,
    val flashcardsCount: Int,
    val exercisesCount: Int,
    val summaryFr: String,
    val summaryEn: String,
    val summaryAr: String,
    val downloadedAtMillis: Long = System.currentTimeMillis(),
    val syncedToCloud: Boolean = true
)

@Entity(tableName = "offline_pending_scores")
data class OfflinePendingScoreEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val moduleType: String,
    val itemId: String,
    val title: String,
    val cefrLevel: String,
    val score: Int,
    val maxScore: Int,
    val xpEarned: Int,
    val feedback: String = "",
    val syncedToCloud: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_guest_profile")
data class LocalGuestProfileEntity(
    @PrimaryKey val id: String = "guest_local",
    val displayName: String = "Invité 👤",
    val cefrLevel: String = "A1",
    val uiLanguage: String = "fr",
    val xp: Long = 50L,
    val streakDays: Long = 1L,
    val hearts: Int = 5,
    val dailyGoalMinutes: Long = 15L,
    val dailyMinutesCompleted: Long = 5L,
    val wordsLearned: Long = 3L,
    val lessonsCompleted: Long = 0L,
    val grammarScore: Long = 50L,
    val pronunciationScore: Long = 50L,
    val listeningScore: Long = 50L,
    val writingScore: Long = 50L,
    val placementCompleted: Boolean = false,
    val selectedTrackId: String = "track_general",
    val completedUnitIdsCsv: String = "",
    val migratedToUid: String = "",
    val xpAlreadyMigrated: Long = 0L,
    val lessonsAlreadyMigrated: Long = 0L,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_guest_flashcards")
data class LocalGuestFlashcardEntity(
    @PrimaryKey val cardId: String,
    val word: String,
    val translationFr: String,
    val translationAr: String,
    val phonetic: String,
    val example: String,
    val category: String,
    val cefrLevel: String,
    val isFavorite: Boolean = true,
    val boxLevel: Long = 1L,
    val nextReviewEpochDay: Long = 0L,
    val mastered: Boolean = false,
    val migratedToCloud: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface OfflineDao {
    // --- Offline Lesson Packs ---
    @Query("SELECT * FROM offline_packs ORDER BY downloadedAtMillis DESC")
    fun observeDownloadedPacks(): Flow<List<OfflinePackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePack(pack: OfflinePackEntity)

    @Query("DELETE FROM offline_packs WHERE packId = :packId")
    suspend fun removePack(packId: String)

    // --- Offline & Guest Progress History ---
    @Query("SELECT * FROM offline_pending_scores ORDER BY createdAtMillis DESC")
    fun observePendingScores(): Flow<List<OfflinePendingScoreEntity>>

    @Query("SELECT * FROM offline_pending_scores WHERE syncedToCloud = 0 ORDER BY createdAtMillis ASC")
    suspend fun getPendingScoresList(): List<OfflinePendingScoreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun queuePendingScore(score: OfflinePendingScoreEntity)

    @Query("UPDATE offline_pending_scores SET syncedToCloud = 1 WHERE localId = :localId")
    suspend fun markScoreSynced(localId: Int)

    @Query("DELETE FROM offline_pending_scores WHERE localId = :localId")
    suspend fun deletePendingScore(localId: Int)

    // --- Local Guest Profile (MODE INVITÉ 👤) ---
    @Query("SELECT * FROM local_guest_profile WHERE id = 'guest_local' LIMIT 1")
    fun observeGuestProfile(): Flow<LocalGuestProfileEntity?>

    @Query("SELECT * FROM local_guest_profile WHERE id = 'guest_local' LIMIT 1")
    suspend fun getGuestProfileOnce(): LocalGuestProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGuestProfile(profile: LocalGuestProfileEntity)

    // --- Local Guest Flashcards (SRS in Guest Mode) ---
    @Query("SELECT * FROM local_guest_flashcards ORDER BY updatedAtMillis DESC")
    fun observeGuestFlashcards(): Flow<List<LocalGuestFlashcardEntity>>

    @Query("SELECT * FROM local_guest_flashcards")
    suspend fun getGuestFlashcardsOnce(): List<LocalGuestFlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGuestFlashcard(card: LocalGuestFlashcardEntity)

    @Query("UPDATE local_guest_flashcards SET migratedToCloud = 1 WHERE cardId = :cardId")
    suspend fun markGuestFlashcardMigrated(cardId: String)
}

@Database(
    entities = [
        OfflinePackEntity::class,
        OfflinePendingScoreEntity::class,
        LocalGuestProfileEntity::class,
        LocalGuestFlashcardEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getInstance(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "english_master_offline.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { INSTANCE = it }
            }
        }
    }
}
