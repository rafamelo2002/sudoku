package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
  @PrimaryKey val id: String = "stats_summary",
  val gamesPlayed: Int = 0,
  val gamesWon: Int = 0,
  val bestTimeSecondsEasy: Int = 0,
  val bestTimeSecondsMedium: Int = 0,
  val bestTimeSecondsHard: Int = 0,
  val bestTimeSecondsExpert: Int = 0,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val updatedAt: Long = System.currentTimeMillis()
)
