package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class GameSession(
  @DocumentId val id: String = "",
  val userId: String = "",
  val difficulty: String = "EASY",
  val initialBoard: String = "",
  val currentBoard: String = "",
  val solution: String = "",
  val notes: String = "{}",
  val elapsedSeconds: Int = 0,
  val mistakes: Int = 0,
  val maxMistakes: Int = 3,
  val isCompleted: Boolean = false,
  val isPaused: Boolean = false,
  val hintsUsed: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
