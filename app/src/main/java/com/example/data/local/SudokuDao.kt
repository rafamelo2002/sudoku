package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {

  // --- Active Game Session Queries ---
  @Query("SELECT * FROM game_sessions WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
  fun observeActiveGame(): Flow<GameSessionEntity?>

  @Query("SELECT * FROM game_sessions WHERE id = :id LIMIT 1")
  suspend fun getGameById(id: String): GameSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveGame(game: GameSessionEntity)

  @Query("DELETE FROM game_sessions WHERE id = :id")
  suspend fun deleteGame(id: String)

  @Query("DELETE FROM game_sessions WHERE isCompleted = 0")
  suspend fun clearActiveGames()

  // --- User Stats Queries ---
  @Query("SELECT * FROM user_stats WHERE id = 'stats_summary' LIMIT 1")
  fun observeUserStats(): Flow<UserStatsEntity?>

  @Query("SELECT * FROM user_stats WHERE id = 'stats_summary' LIMIT 1")
  suspend fun getUserStats(): UserStatsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUserStats(stats: UserStatsEntity)

  // --- Player Profile Queries ---
  @Query("SELECT * FROM player_profile WHERE id = 'player_main' LIMIT 1")
  fun observePlayerProfile(): Flow<PlayerProfileEntity?>

  @Query("SELECT * FROM player_profile WHERE id = 'player_main' LIMIT 1")
  suspend fun getPlayerProfile(): PlayerProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun savePlayerProfile(profile: PlayerProfileEntity)

  // --- Stage Progress Queries ---
  @Query("SELECT * FROM stage_progress")
  fun observeAllStageProgress(): Flow<List<StageProgressEntity>>

  @Query("SELECT * FROM stage_progress WHERE stageId = :stageId LIMIT 1")
  suspend fun getStageProgress(stageId: String): StageProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveStageProgress(progress: StageProgressEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveAllStageProgress(progressList: List<StageProgressEntity>)

  // --- Achievement Progress Queries ---
  @Query("SELECT * FROM achievement_progress")
  fun observeAllAchievements(): Flow<List<AchievementProgressEntity>>

  @Query("SELECT * FROM achievement_progress WHERE achievementId = :id LIMIT 1")
  suspend fun getAchievement(id: String): AchievementProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveAchievement(achievement: AchievementProgressEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveAllAchievements(achievements: List<AchievementProgressEntity>)

  // --- Character Unlocks Queries ---
  @Query("SELECT * FROM character_unlocks")
  fun observeAllCharacterUnlocks(): Flow<List<CharacterUnlockEntity>>

  @Query("SELECT * FROM character_unlocks WHERE characterId = :id LIMIT 1")
  suspend fun getCharacterUnlock(id: String): CharacterUnlockEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveCharacterUnlock(characterUnlock: CharacterUnlockEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveAllCharacterUnlocks(unlocks: List<CharacterUnlockEntity>)
}
