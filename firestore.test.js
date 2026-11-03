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

function validGameSession(userId) {
  const zeros = "0".repeat(81);
  const ones = "1".repeat(81);
  return {
    userId,
    difficulty: "EASY",
    initialBoard: zeros,
    currentBoard: zeros,
    solution: ones,
    notes: "{}",
    elapsedSeconds: 30,
    mistakes: 0,
    maxMistakes: 3,
    isCompleted: false,
    isPaused: false,
    hintsUsed: 0,
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

function validUserStats(userId) {
  return {
    userId,
    gamesPlayed: 1,
    gamesWon: 1,
    bestTimeSecondsEasy: 120,
    bestTimeSecondsMedium: 0,
    bestTimeSecondsHard: 0,
    bestTimeSecondsExpert: 0,
    currentStreak: 1,
    bestStreak: 1,
    updatedAt: new Date(),
  };
}

test("Unauthenticated user: cannot read games", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("games").get());
});

test("Authenticated user: can create their own valid game", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const gameRef = aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_1");
  await assertSucceeds(gameRef.set(validGameSession(ALICE_UID)));
});

test("Authenticated user: cannot create game for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobGameRef = aliceDb.collection("users").doc(BOB_UID).collection("games").doc("game_2");
  await assertFails(bobGameRef.set(validGameSession(BOB_UID)));
});

test("Authenticated user: cannot read another user's games", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("games").doc("game_bob").set(validGameSession(BOB_UID));
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("games").doc("game_bob").get());
});

test("Authenticated user: can update currentBoard and elapsedSeconds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const gameRef = aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_1");
  const initial = validGameSession(ALICE_UID);
  await gameRef.set(initial);

  await assertSucceeds(
    gameRef.update({
      currentBoard: "1" + "0".repeat(80),
      elapsedSeconds: 45,
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot update immutable initialBoard", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const gameRef = aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_1");
  await gameRef.set(validGameSession(ALICE_UID));

  await assertFails(
    gameRef.update({
      initialBoard: "1".repeat(81),
      updatedAt: new Date(),
    })
  );
});

test("UserStats: create and update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const statsRef = aliceDb.collection("users").doc(ALICE_UID).collection("stats").doc("summary");
  await assertSucceeds(statsRef.set(validUserStats(ALICE_UID)));

  await assertSucceeds(
    statsRef.update({
      gamesPlayed: 2,
      gamesWon: 2,
      bestTimeSecondsEasy: 110,
      currentStreak: 2,
      bestStreak: 2,
      updatedAt: new Date(),
    })
  );
});
