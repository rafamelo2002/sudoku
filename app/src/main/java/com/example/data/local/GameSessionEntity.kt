package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
  @PrimaryKey val id: String = "active_game",
  val stageId: String? = null,
  val isDaily: Boolean = false,
  val difficulty: String = "EASY",
  val initialBoard: String = "",
  val currentBoard: String = "",
  val solution: String = "",
  val notes: String = "",
  val elapsedSeconds: Int = 0,
  val mistakes: Int = 0,
  val maxMistakes: Int = 3,
  val isCompleted: Boolean = false,
  val isPaused: Boolean = false,
  val hintsUsed: Int = 0,
  val updatedAt: Long = System.currentTimeMillis()
)
