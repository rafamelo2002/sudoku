package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
  @PrimaryKey val id: String = "player_main",
  val name: String = "Aventureiro",
  val level: Int = 1,
  val currentXp: Int = 0,
  val coins: Int = 50,
  val dailyStreak: Int = 1,
  val lastDailyDate: String = "",
  val selectedCharacterId: String = "hikari",
  val stagesCompletedCount: Int = 0,
  val updatedAt: Long = System.currentTimeMillis()
)
