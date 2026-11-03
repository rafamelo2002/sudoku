package com.example.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.GameSessionEntity
import com.example.data.local.SudokuDao
import com.example.data.local.UserStatsEntity
import kotlinx.coroutines.flow.Flow

class SudokuRepository(private val dao: SudokuDao) {

  constructor(context: Context) : this(
    AppDatabase.getInstance(context).sudokuDao()
  )

  fun observeActiveGame(): Flow<GameSessionEntity?> = dao.observeActiveGame()

  fun observeUserStats(): Flow<UserStatsEntity?> = dao.observeUserStats()

  suspend fun saveNewGame(game: GameSessionEntity): Result<String> {
    return try {
      dao.saveGame(game)
      Result.success(game.id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateGame(game: GameSessionEntity): Result<Unit> {
    return try {
      dao.saveGame(game.copy(updatedAt = System.currentTimeMillis()))
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteGame(gameId: String): Result<Unit> {
    return try {
      dao.deleteGame(gameId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun saveUserStats(stats: UserStatsEntity): Result<Unit> {
    return try {
      dao.saveUserStats(stats.copy(updatedAt = System.currentTimeMillis()))
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
