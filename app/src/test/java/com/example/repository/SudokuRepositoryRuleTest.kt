package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.GameSessionEntity
import com.example.data.local.UserStatsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SudokuRepositoryRuleTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: SudokuRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = SudokuRepository(db.sudokuDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun saveNewGame_and_updateGame_success() = runBlocking {
    val zeros = "0".repeat(81)
    val ones = "1".repeat(81)
    val game = GameSessionEntity(
      id = "active_game",
      difficulty = "EASY",
      initialBoard = zeros,
      currentBoard = zeros,
      solution = ones,
      notes = "",
      elapsedSeconds = 10,
      mistakes = 0,
      maxMistakes = 3,
      isCompleted = false,
      isPaused = false,
      hintsUsed = 0
    )

    val saveResult = repository.saveNewGame(game)
    assertTrue("Save game should succeed", saveResult.isSuccess)

    val retrieved = repository.observeActiveGame().first()
    assertNotNull("Saved game should be retrieved", retrieved)
    assertEquals(10, retrieved?.elapsedSeconds)

    val updatedGame = game.copy(
      currentBoard = "1" + "0".repeat(80),
      elapsedSeconds = 25
    )
    val updateResult = repository.updateGame(updatedGame)
    assertTrue("Update game should succeed", updateResult.isSuccess)

    val updated = repository.observeActiveGame().first()
    assertEquals(25, updated?.elapsedSeconds)
  }

  @Test
  fun saveUserStats_and_retrieve_success() = runBlocking {
    val stats = UserStatsEntity(
      id = "stats_summary",
      gamesPlayed = 3,
      gamesWon = 2,
      bestTimeSecondsEasy = 150,
      bestTimeSecondsMedium = 200,
      bestTimeSecondsHard = 0,
      bestTimeSecondsExpert = 0,
      currentStreak = 2,
      bestStreak = 2
    )

    val saveResult = repository.saveUserStats(stats)
    assertTrue("Save stats should succeed", saveResult.isSuccess)

    val retrieved = repository.observeUserStats().first()
    assertNotNull("Saved stats should be retrieved", retrieved)
    assertEquals(3, retrieved?.gamesPlayed)
    assertEquals(2, retrieved?.gamesWon)
  }
}
