package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [36])
class EnglishMasterRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndReadProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = EnglishMasterRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createInitialProfile(
                displayName = "Alice",
                email = ALICE_EMAIL,
                cefrLevel = "B1",
                uiLanguage = "fr",
                dailyGoalMinutes = 20L
            )
        }
        assertTrue(result.isSuccess)
        assertEquals(uid, result.getOrThrow())

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getUserProfileById(uid).getOrThrow()
        }
        assertNotNull(fetched)
        assertEquals("B1", fetched?.cefrLevel)
    }

    @Test
    fun getProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = EnglishMasterRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createInitialProfile("Alice", ALICE_EMAIL, "B2", "fr", 15L).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = EnglishMasterRepository(firestore)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserProfileById(aliceUid)
        }
        assertTrue(crossResult.isFailure)
        val firestoreEx = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun flashcardsAndProgress_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = EnglishMasterRepository(firestore)

        val cardId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createFlashcard(
                word = "ubiquitous",
                translationFr = "omniprésent",
                translationAr = "منتشر في كل مكان",
                phonetic = "/juːˈbɪkwɪtəs/",
                example = "Smartphones are ubiquitous today.",
                category = "Academic",
                cefrLevel = "C1"
            ).getOrThrow()
        }

        val cards = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeFlashcards(uid).first { list -> list.any { it.id == cardId } }
        }
        assertTrue(cards.any { it.id == cardId && it.word == "ubiquitous" })
    }

    @Test
    fun observeFlashcards_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = EnglishMasterRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeFlashcards(aliceUid).first()
            }
            fail("Expected exception for unauthenticated flow observation")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_english@test.com"
        const val BOB_EMAIL = "bob_english@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
