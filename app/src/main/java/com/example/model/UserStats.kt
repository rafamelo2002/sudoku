package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserStats(
  @DocumentId val id: String = "",
  val userId: String = "",
  val gamesPlayed: Int = 0,
  val gamesWon: Int = 0,
  val bestTimeSecondsEasy: Int = 0,
  val bestTimeSecondsMedium: Int = 0,
  val bestTimeSecondsHard: Int = 0,
  val bestTimeSecondsExpert: Int = 0,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
