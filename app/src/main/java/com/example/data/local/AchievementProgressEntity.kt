package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
  @PrimaryKey val achievementId: String,
  val currentProgress: Int = 0,
  val isClaimed: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis()
)
