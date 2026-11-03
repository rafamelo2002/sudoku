package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuestConfig
import com.example.repository.QuestRepository
import com.example.repository.SudokuRepository
import com.example.ui.collection.CollectionScreen
import com.example.ui.daily.DailyChallengeScreen
import com.example.ui.game.GameScreen
import com.example.ui.game.SudokuViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.map.AdventureMapScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.SudokuTheme

enum class AppScreen {
  HOME,
  MAP,
  DAILY,
  COLLECTION,
  GAME,
  STATS
}

class MainActivity : ComponentActivity() {

  private val sudokuViewModel by viewModels<SudokuViewModel>(
    factoryProducer = {
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val sudokuRepository = SudokuRepository(applicationContext)
          val questRepository = QuestRepository(applicationContext)
          return SudokuViewModel(sudokuRepository, questRepository) as T
        }
      }
    }
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      SudokuTheme {
        SudokuApp(sudokuViewModel = sudokuViewModel)
      }
    }
  }
}

@Composable
fun SudokuApp(
  sudokuViewModel: SudokuViewModel
) {
  var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

  val activeGame by sudokuViewModel.activeSavedGame.collectAsStateWithLifecycle()
  val userStats by sudokuViewModel.userStats.collectAsStateWithLifecycle()
  val gameUiState by sudokuViewModel.uiState.collectAsStateWithLifecycle()
  val playerProfile by sudokuViewModel.playerProfile.collectAsStateWithLifecycle()
  val selectedCompanion by sudokuViewModel.selectedCompanion.collectAsStateWithLifecycle()
  val stageProgressMap by sudokuViewModel.stageProgressMap.collectAsStateWithLifecycle()
  val achievementsMap by sudokuViewModel.achievementsMap.collectAsStateWithLifecycle()
  val unlockedCharacters by sudokuViewModel.unlockedCharacters.collectAsStateWithLifecycle()
  val rewardSummary by sudokuViewModel.lastRewardSummary.collectAsStateWithLifecycle()

  Surface(modifier = Modifier.fillMaxSize()) {
    when (currentScreen) {
      AppScreen.HOME -> {
        HomeScreen(
          playerProfile = playerProfile,
          companion = selectedCompanion,
          activeGame = activeGame,
          userStats = userStats,
          isLoading = gameUiState.isLoading,
          onContinueAdventure = {
            if (activeGame != null && !activeGame!!.isCompleted) {
              sudokuViewModel.resumeActiveGame(activeGame!!)
              currentScreen = AppScreen.GAME
            } else {
              val allStages: List<com.example.data.model.Stage> = QuestConfig.ALL_CHAPTERS.flatMap { it.stages }
              val targetStage = allStages.firstOrNull { stage ->
                val progress = stageProgressMap[stage.id]
                val unlocked = progress?.isUnlocked ?: (stage.id == "c1_s1")
                val completed = progress?.isCompleted ?: false
                unlocked && !completed
              } ?: allStages.first()

              sudokuViewModel.startStage(targetStage)
              currentScreen = AppScreen.GAME
            }
          },
          onOpenMap = {
            currentScreen = AppScreen.MAP
          },
          onOpenDaily = {
            currentScreen = AppScreen.DAILY
          },
          onOpenCollection = {
            currentScreen = AppScreen.COLLECTION
          },
          onStartClassicGame = { difficulty ->
            sudokuViewModel.startNewGame(difficulty)
            currentScreen = AppScreen.GAME
          },
          onViewStats = {
            currentScreen = AppScreen.STATS
          }
        )
      }

      AppScreen.MAP -> {
        AdventureMapScreen(
          stageProgressMap = stageProgressMap,
          onBack = { currentScreen = AppScreen.HOME },
          onSelectStage = { stage ->
            sudokuViewModel.startStage(stage)
            currentScreen = AppScreen.GAME
          }
        )
      }

      AppScreen.DAILY -> {
        DailyChallengeScreen(
          playerProfile = playerProfile,
          onStartDaily = {
            sudokuViewModel.startDailyChallenge()
            currentScreen = AppScreen.GAME
          },
          onBack = { currentScreen = AppScreen.HOME }
        )
      }

      AppScreen.COLLECTION -> {
        CollectionScreen(
          playerProfile = playerProfile,
          unlockedCharacters = unlockedCharacters,
          achievementsMap = achievementsMap,
          onSelectCharacter = { charId ->
            sudokuViewModel.selectCharacter(charId)
          },
          onClaimAchievement = { achId ->
            sudokuViewModel.claimAchievement(achId)
          },
          onBack = { currentScreen = AppScreen.HOME }
        )
      }

      AppScreen.GAME -> {
        GameScreen(
          uiState = gameUiState,
          companion = selectedCompanion,
          rewardSummary = rewardSummary,
          onCellClick = { r, c -> sudokuViewModel.selectCell(r, c) },
          onNumberInput = { num -> sudokuViewModel.inputNumber(num) },
          onErase = { sudokuViewModel.eraseCell() },
          onUndo = { sudokuViewModel.undo() },
          onToggleNotes = { sudokuViewModel.toggleNotesMode() },
          onUseHint = { sudokuViewModel.useHint() },
          onPause = { sudokuViewModel.pauseGame() },
          onResume = { sudokuViewModel.resumeGame() },
          onNewGame = { difficulty -> sudokuViewModel.startNewGame(difficulty) },
          onNextStage = if (gameUiState.stageId != null) {
            {
              val allStages: List<com.example.data.model.Stage> = QuestConfig.ALL_CHAPTERS.flatMap { it.stages }
              val currentIdx = allStages.indexOfFirst { it.id == gameUiState.stageId }
              if (currentIdx in 0 until (allStages.size - 1)) {
                val nextStageItem = allStages[currentIdx + 1]
                sudokuViewModel.startStage(nextStageItem)
              } else {
                currentScreen = AppScreen.MAP
              }
            }
          } else null,
          onBackToMenu = {
            currentScreen = AppScreen.HOME
          }
        )
      }

      AppScreen.STATS -> {
        StatsScreen(
          userStats = userStats,
          onBack = { currentScreen = AppScreen.HOME }
        )
      }
    }
  }
}
