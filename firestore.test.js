const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user can create and read their own valid UserProfile, but Bob cannot read Alice's profile (PII isolation)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Learner",
      email: "alice@example.com",
      avatarTag: "scholar",
      cefrLevel: "B1",
      uiLanguage: "fr",
      xp: 250,
      streakDays: 5,
      dailyGoalMinutes: 15,
      dailyMinutesCompleted: 10,
      wordsLearned: 42,
      lessonsCompleted: 8,
      grammarScore: 78,
      pronunciationScore: 82,
      listeningScore: 80,
      writingScore: 75,
      placementCompleted: true,
      subscriptionTier: "Free",
      selectedTrackId: "track_general",
      createdAt: now,
      updatedAt: now,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Shadow update with ghost field on UserProfile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Learner",
      cefrLevel: "A2",
      uiLanguage: "fr",
      xp: 100,
      streakDays: 2,
      dailyGoalMinutes: 15,
      dailyMinutesCompleted: 5,
      wordsLearned: 10,
      lessonsCompleted: 2,
      grammarScore: 60,
      pronunciationScore: 65,
      listeningScore: 70,
      writingScore: 60,
      placementCompleted: true,
      subscriptionTier: "Free",
      selectedTrackId: "track_general",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 200,
      updatedAt: new Date(),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 200,
      isSuperAdminGhost: true,
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user can manage their own flashcards and progress, and cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1").set({
      userId: ALICE_UID,
      word: "resilient",
      translationFr: "résilient",
      translationAr: "مرن",
      phonetic: "/rɪˈzɪliənt/",
      example: "She is a resilient leader.",
      category: "Business",
      cefrLevel: "B2",
      isFavorite: true,
      boxLevel: 2,
      nextReviewEpochDay: 20000,
      mastered: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("flashcards")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Leaderboard enforces query alignment (xp >= 0) and owner-only writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      cefrLevel: "B2",
      xp: 1200,
      streakDays: 14,
      badgesCount: 4,
      updatedAt: now,
    })
  );

  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).update({
      xp: 999999,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("leaderboard").where("xp", ">=", 0).get()
  );
});

test("Catalog items can be created by author and queried when published == true", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("custom_catalog").doc("item_1").set({
      authorId: ALICE_UID,
      itemType: "vocabulary",
      title: "Break the ice",
      subtitle: "Idiom for starting a conversation",
      cefrLevel: "B1",
      contentBody: "Used in social and business introductions.",
      translationFr: "Briser la glace",
      translationAr: "كسر الجليد",
      quizQuestion: "What does 'break the ice' mean?",
      quizOptionsCsv: "Start a conversation,Freeze water,Cancel a meeting,Leave early",
      quizAnswer: "Start a conversation",
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    bobDb.collection("custom_catalog").where("published", "==", true).get()
  );
});

test("Community messages allow authenticated users to post, query by roomId, and submit peer grammar corrections without altering sender or original content", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("community_messages").doc("cmsg_1").set({
      senderId: ALICE_UID,
      senderName: "Alice",
      senderCefr: "B1",
      roomId: "general",
      recipientId: "",
      recipientName: "",
      content: "Hello everyone! I am practice English today.",
      correctionSuggestion: "",
      correctedByName: "",
      likesCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can read messages in the 'general' room
  await assertSucceeds(
    bobDb.collection("community_messages").where("roomId", "==", "general").get()
  );

  // Bob can submit a peer correction and increment likesCount
  await assertSucceeds(
    bobDb.collection("community_messages").doc("cmsg_1").update({
      correctionSuggestion: "Hello everyone! I am practicing English today.",
      correctedByName: "Bob",
      likesCount: 1,
      updatedAt: new Date(),
    })
  );

  // Bob CANNOT alter Alice's original message content or senderId
  await assertFails(
    bobDb.collection("community_messages").doc("cmsg_1").update({
      content: "Tampered message content",
      updatedAt: new Date(),
    })
  );

  // Bob CANNOT delete Alice's message
  await assertFails(
    bobDb.collection("community_messages").doc("cmsg_1").delete()
  );
});

