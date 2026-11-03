package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stage_progress")
data class StageProgressEntity(
  @PrimaryKey val stageId: String,
  val chapterId: Int,
  val stageNumber: Int,
  val isUnlocked: Boolean = false,
  val isCompleted: Boolean = false,
  val stars: Int = 0,
  val bestTimeSeconds: Int = 0,
  val updatedAt: Long = System.currentTimeMillis()
)
