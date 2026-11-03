package com.example.repository

import android.content.Context
import com.example.data.local.AchievementProgressEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CharacterUnlockEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.StageProgressEntity
import com.example.data.local.SudokuDao
import com.example.data.model.CompanionCharacter
import com.example.data.model.QuestConfig
import com.example.data.model.RewardSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuestRepository(private val dao: SudokuDao) {

  constructor(context: Context) : this(
    AppDatabase.getInstance(context).sudokuDao()
  )

  suspend fun ensureInitialized() {
    val existingProfile = dao.getPlayerProfile()
    if (existingProfile == null) {
      dao.savePlayerProfile(
        PlayerProfileEntity(
          id = "player_main",
          name = "Aventureiro",
          level = 1,
          currentXp = 0,
          coins = 50,
          dailyStreak = 1,
          lastDailyDate = "",
          selectedCharacterId = "hikari",
          stagesCompletedCount = 0
        )
      )
    }

    val unlockedHikari = dao.getCharacterUnlock("hikari")
    if (unlockedHikari == null) {
      dao.saveCharacterUnlock(
        CharacterUnlockEntity(characterId = "hikari", isUnlocked = true)
      )
    }

    val c1s1 = dao.getStageProgress("c1_s1")
    if (c1s1 == null) {
      val initialStages = mutableListOf<StageProgressEntity>()
      for (chapter in QuestConfig.CHAPTERS) {
        for (stage in chapter.stages) {
          initialStages.add(
            StageProgressEntity(
              stageId = stage.id,
              chapterId = stage.chapterId,
              stageNumber = stage.stageNumber,
              isUnlocked = (stage.id == "c1_s1"),
              isCompleted = false,
              stars = 0,
              bestTimeSeconds = 0
            )
          )
        }
      }
      dao.saveAllStageProgress(initialStages)
    }

    val firstAch = dao.getAchievement(QuestConfig.ALL_ACHIEVEMENTS.first().id)
    if (firstAch == null) {
      val initialAchievements = QuestConfig.ALL_ACHIEVEMENTS.map { def ->
        AchievementProgressEntity(
          achievementId = def.id,
          currentProgress = 0,
          isClaimed = false
        )
      }
      dao.saveAllAchievements(initialAchievements)
    }
  }

  fun observePlayerProfile(): Flow<PlayerProfileEntity> =
    dao.observePlayerProfile().map { it ?: PlayerProfileEntity() }

  fun observeAllStageProgress(): Flow<Map<String, StageProgressEntity>> =
    dao.observeAllStageProgress().map { list -> list.associateBy { it.stageId } }

  fun observeAllAchievements(): Flow<Map<String, AchievementProgressEntity>> =
    dao.observeAllAchievements().map { list -> list.associateBy { it.achievementId } }

  fun observeUnlockedCharacters(): Flow<Set<String>> =
    dao.observeAllCharacterUnlocks().map { list ->
      list.filter { it.isUnlocked }.map { it.characterId }.toSet().ifEmpty { setOf("hikari") }
    }

  suspend fun selectCompanion(characterId: String) {
    val current = dao.getPlayerProfile() ?: PlayerProfileEntity()
    dao.savePlayerProfile(current.copy(selectedCharacterId = characterId))
  }

  suspend fun completeStage(
    stageId: String,
    stars: Int,
    timeSeconds: Int,
    mistakes: Int,
    hintsUsed: Int
  ): RewardSummary {
    val allStagesFlat = QuestConfig.CHAPTERS.flatMap { it.stages }
    val stageDef = allStagesFlat.find { it.id == stageId }

    val baseExp = stageDef?.xpReward ?: QuestConfig.XP_MEDIUM
    val baseCoins = stageDef?.coinsReward ?: QuestConfig.COINS_MEDIUM

    val starBonusXp = if (stars == 3) 40 else if (stars == 2) 15 else 0
    val starBonusCoins = if (stars == 3) 15 else if (stars == 2) 5 else 0

    val totalXp = baseExp + starBonusXp
    val totalCoins = baseCoins + starBonusCoins

    val player = dao.getPlayerProfile() ?: PlayerProfileEntity()
    var level = player.level
    var currentXp = player.currentXp + totalXp
    var didLevelUp = false

    while (currentXp >= QuestConfig.xpForLevel(level)) {
      currentXp -= QuestConfig.xpForLevel(level)
      level++
      didLevelUp = true
    }

    val updatedPlayer = player.copy(
      level = level,
      currentXp = currentXp,
      coins = player.coins + totalCoins,
      stagesCompletedCount = player.stagesCompletedCount + 1
    )
    dao.savePlayerProfile(updatedPlayer)

    val existingProgress = dao.getStageProgress(stageId)
    val updatedStars = maxOf(existingProgress?.stars ?: 0, stars)
    val bestTime = if (existingProgress != null && existingProgress.bestTimeSeconds > 0) {
      minOf(existingProgress.bestTimeSeconds, timeSeconds)
    } else {
      timeSeconds
    }

    val updatedStageProgress = StageProgressEntity(
      stageId = stageId,
      chapterId = stageDef?.chapterId ?: 1,
      stageNumber = stageDef?.stageNumber ?: 1,
      isUnlocked = true,
      isCompleted = true,
      stars = updatedStars,
      bestTimeSeconds = bestTime
    )
    dao.saveStageProgress(updatedStageProgress)

    var nextStageId: String? = null
    val currentIndex = allStagesFlat.indexOfFirst { it.id == stageId }
    if (currentIndex != -1 && currentIndex + 1 < allStagesFlat.size) {
      val nextStage = allStagesFlat[currentIndex + 1]
      nextStageId = nextStage.id
      val nextExisting = dao.getStageProgress(nextStage.id)
      if (nextExisting == null || !nextExisting.isUnlocked) {
        dao.saveStageProgress(
          StageProgressEntity(
            stageId = nextStage.id,
            chapterId = nextStage.chapterId,
            stageNumber = nextStage.stageNumber,
            isUnlocked = true,
            isCompleted = nextExisting?.isCompleted ?: false,
            stars = nextExisting?.stars ?: 0,
            bestTimeSeconds = nextExisting?.bestTimeSeconds ?: 0
          )
        )
      }
    }

    var newlyUnlockedCharacter: CompanionCharacter? = null
    if (stageDef?.isBoss == true) {
      val charToUnlock = when (stageDef.chapterId) {
        1 -> "ren"
        2 -> "kael"
        3 -> "aria"
        else -> null
      }
      if (charToUnlock != null) {
        val unlockRec = dao.getCharacterUnlock(charToUnlock)
        if (unlockRec == null || !unlockRec.isUnlocked) {
          dao.saveCharacterUnlock(CharacterUnlockEntity(characterId = charToUnlock, isUnlocked = true))
          newlyUnlockedCharacter = QuestConfig.ALL_CHARACTERS.find { it.id == charToUnlock }
        }
      }
    }

    recordAchievementProgress("first_win", 1)
    if (mistakes == 0) recordAchievementProgress("flawless_win", 1)
    if (stars == 3) recordAchievementProgress("three_stars", 1)
    if (timeSeconds < 180) recordAchievementProgress("fast_solver", 1)
    if (stageDef?.isBoss == true) recordAchievementProgress("boss_slayer", 1)
    if (stageDef?.difficulty == com.example.game.Difficulty.HARD || stageDef?.difficulty == com.example.game.Difficulty.EXPERT) {
      recordAchievementProgress("hard_master", 1)
    }
    recordAchievementProgress("puzzles_10", updatedPlayer.stagesCompletedCount)

    if (stageDef?.chapterId == 1 && stageDef.stageNumber == 5) {
      recordAchievementProgress("chapter_1_clear", 5)
    }

    return RewardSummary(
      xpEarned = totalXp,
      coinsEarned = totalCoins,
      stars = stars,
      didLevelUp = didLevelUp,
      newLevel = level,
      unlockedNextStageId = nextStageId,
      newlyUnlockedCharacter = newlyUnlockedCharacter
    )
  }

  suspend fun completeDailyChallenge(timeSeconds: Int): RewardSummary {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val player = dao.getPlayerProfile() ?: PlayerProfileEntity()

    val streak = if (player.lastDailyDate != today) {
      player.dailyStreak + 1
    } else {
      player.dailyStreak
    }

    val totalXp = QuestConfig.XP_DAILY + (streak * 10)
    val totalCoins = QuestConfig.COINS_DAILY + (streak * 5)

    var level = player.level
    var currentXp = player.currentXp + totalXp
    var didLevelUp = false

    while (currentXp >= QuestConfig.xpForLevel(level)) {
      currentXp -= QuestConfig.xpForLevel(level)
      level++
      didLevelUp = true
    }

    dao.savePlayerProfile(
      player.copy(
        level = level,
        currentXp = currentXp,
        coins = player.coins + totalCoins,
        dailyStreak = streak,
        lastDailyDate = today
      )
    )

    recordAchievementProgress("daily_streak_3", streak)

    return RewardSummary(
      xpEarned = totalXp,
      coinsEarned = totalCoins,
      stars = 3,
      didLevelUp = didLevelUp,
      newLevel = level
    )
  }

  private suspend fun recordAchievementProgress(achievementId: String, incrementOrValue: Int) {
    val def = QuestConfig.ALL_ACHIEVEMENTS.find { it.id == achievementId } ?: return
    val current = dao.getAchievement(achievementId) ?: AchievementProgressEntity(achievementId)
    val newProgress = maxOf(current.currentProgress, incrementOrValue)
    dao.saveAchievement(current.copy(currentProgress = newProgress))
  }

  suspend fun claimAchievement(achievementId: String): Pair<Int, Int>? {
    val def = QuestConfig.ALL_ACHIEVEMENTS.find { it.id == achievementId } ?: return null
    val current = dao.getAchievement(achievementId) ?: return null
    if (current.isClaimed || current.currentProgress < def.target) return null

    dao.saveAchievement(current.copy(isClaimed = true))

    val player = dao.getPlayerProfile() ?: PlayerProfileEntity()
    var level = player.level
    var currentXp = player.currentXp + def.rewardXp

    while (currentXp >= QuestConfig.xpForLevel(level)) {
      currentXp -= QuestConfig.xpForLevel(level)
      level++
    }

    dao.savePlayerProfile(
      player.copy(
        level = level,
        currentXp = currentXp,
        coins = player.coins + def.rewardCoins
      )
    )

    return Pair(def.rewardXp, def.rewardCoins)
  }
}
