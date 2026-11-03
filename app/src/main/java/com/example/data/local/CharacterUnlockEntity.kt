package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_unlocks")
data class CharacterUnlockEntity(
  @PrimaryKey val characterId: String,
  val isUnlocked: Boolean = false,
  val unlockedAt: Long = System.currentTimeMillis()
)
