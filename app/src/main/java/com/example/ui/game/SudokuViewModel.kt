package com.example.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AchievementProgressEntity
import com.example.data.local.GameSessionEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.StageProgressEntity
import com.example.data.local.UserStatsEntity
import com.example.data.model.CompanionCharacter
import com.example.data.model.QuestConfig
import com.example.data.model.RewardSummary
import com.example.data.model.Stage
import com.example.game.Difficulty
import com.example.game.SudokuGenerator
import com.example.repository.QuestRepository
import com.example.repository.SudokuRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MoveAction {
  data class ValueChange(val index: Int, val oldChar: Char, val newChar: Char) : MoveAction()
  data class NoteChange(val index: Int, val oldNotes: Set<Int>, val newNotes: Set<Int>) : MoveAction()
}

data class SudokuUiState(
  val gameId: String = "active_game",
  val stageId: String? = null,
  val isDaily: Boolean = false,
  val difficulty: Difficulty = Difficulty.EASY,
  val initialBoard: String = "0".repeat(81),
  val currentBoard: String = "0".repeat(81),
  val solution: String = "1".repeat(81),
  val selectedCell: Pair<Int, Int>? = null, // row, col
  val notes: Map<Int, Set<Int>> = emptyMap(),
  val isNotesMode: Boolean = false,
  val elapsedSeconds: Int = 0,
  val mistakes: Int = 0,
  val maxMistakes: Int = 3,
  val isCompleted: Boolean = false,
  val isGameOver: Boolean = false,
  val isPaused: Boolean = false,
  val hintsUsed: Int = 0,
  val canUndo: Boolean = false,
  val isLoading: Boolean = false,
  val isBossPuzzle: Boolean = false,
  val bossName: String? = null,
  val lastCompletedSection: String? = null // for row/col/block completion animation
)

class SudokuViewModel(
  private val sudokuRepository: SudokuRepository,
  private val questRepository: QuestRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(SudokuUiState())
  val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

  private val _activeSavedGame = MutableStateFlow<GameSessionEntity?>(null)
  val activeSavedGame: StateFlow<GameSessionEntity?> = _activeSavedGame.asStateFlow()

  private val _userStats = MutableStateFlow<UserStatsEntity?>(null)
  val userStats: StateFlow<UserStatsEntity?> = _userStats.asStateFlow()

  val playerProfile: StateFlow<PlayerProfileEntity> =
    questRepository.observePlayerProfile().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = PlayerProfileEntity()
    )

  val stageProgressMap: StateFlow<Map<String, StageProgressEntity>> =
    questRepository.observeAllStageProgress().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  val achievementsMap: StateFlow<Map<String, AchievementProgressEntity>> =
    questRepository.observeAllAchievements().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  val unlockedCharacters: StateFlow<Set<String>> =
    questRepository.observeUnlockedCharacters().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = setOf("hikari")
    )

  val selectedCompanion: StateFlow<CompanionCharacter> =
    playerProfile.map { profile ->
      QuestConfig.ALL_CHARACTERS.find { it.id == profile.selectedCharacterId }
        ?: QuestConfig.ALL_CHARACTERS.first()
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = QuestConfig.ALL_CHARACTERS.first()
    )

  private val _currentStage = MutableStateFlow<Stage?>(null)
  val currentStage: StateFlow<Stage?> = _currentStage.asStateFlow()

  private val _lastRewardSummary = MutableStateFlow<RewardSummary?>(null)
  val lastRewardSummary: StateFlow<RewardSummary?> = _lastRewardSummary.asStateFlow()

  private val undoStack = mutableListOf<MoveAction>()
  private var timerJob: Job? = null
  private var autoSaveJob: Job? = null

  init {
    viewModelScope.launch {
      questRepository.ensureInitialized()
    }

    viewModelScope.launch {
      sudokuRepository.observeActiveGame()
        .collect { game ->
          _activeSavedGame.value = game
        }
    }

    viewModelScope.launch {
      sudokuRepository.observeUserStats()
        .collect { stats ->
          _userStats.value = stats
        }
    }
  }

  fun startStage(stage: Stage) {
    timerJob?.cancel()
    undoStack.clear()
    _currentStage.value = stage
    _lastRewardSummary.value = null

    _uiState.value = _uiState.value.copy(isLoading = true)

    val puzzle = SudokuGenerator.generate(stage.difficulty)
    val session = GameSessionEntity(
      id = "active_game",
      stageId = stage.id,
      isDaily = false,
      difficulty = stage.difficulty.name,
      initialBoard = puzzle.initialBoard,
      currentBoard = puzzle.initialBoard,
      solution = puzzle.solution,
      notes = "",
      elapsedSeconds = 0,
      mistakes = 0,
      maxMistakes = if (stage.isBoss) 3 else 3,
      isCompleted = false,
      isPaused = false,
      hintsUsed = 0
    )

    viewModelScope.launch {
      sudokuRepository.saveNewGame(session)

      _uiState.value = SudokuUiState(
        gameId = "active_game",
        stageId = stage.id,
        isDaily = false,
        difficulty = stage.difficulty,
        initialBoard = puzzle.initialBoard,
        currentBoard = puzzle.initialBoard,
        solution = puzzle.solution,
        selectedCell = null,
        notes = emptyMap(),
        isNotesMode = false,
        elapsedSeconds = 0,
        mistakes = 0,
        maxMistakes = if (stage.isBoss) 3 else 3,
        isCompleted = false,
        isGameOver = false,
        isPaused = false,
        hintsUsed = 0,
        canUndo = false,
        isLoading = false,
        isBossPuzzle = stage.isBoss,
        bossName = stage.bossName
      )

      startTimer()
      updateGamesPlayedStat()
    }
  }

  fun startDailyChallenge() {
    timerJob?.cancel()
    undoStack.clear()
    _currentStage.value = null
    _lastRewardSummary.value = null

    _uiState.value = _uiState.value.copy(isLoading = true)

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    // Alternate daily difficulty
    val dayNumber = today.hashCode()
    val dailyDiff = if (kotlin.math.abs(dayNumber) % 2 == 0) Difficulty.MEDIUM else Difficulty.HARD

    val puzzle = SudokuGenerator.generate(dailyDiff)
    val session = GameSessionEntity(
      id = "active_game",
      stageId = "daily_$today",
      isDaily = true,
      difficulty = dailyDiff.name,
      initialBoard = puzzle.initialBoard,
      currentBoard = puzzle.initialBoard,
      solution = puzzle.solution,
      notes = "",
      elapsedSeconds = 0,
      mistakes = 0,
      maxMistakes = 3,
      isCompleted = false,
      isPaused = false,
      hintsUsed = 0
    )

    viewModelScope.launch {
      sudokuRepository.saveNewGame(session)

      _uiState.value = SudokuUiState(
        gameId = "active_game",
        stageId = "daily_$today",
        isDaily = true,
        difficulty = dailyDiff,
        initialBoard = puzzle.initialBoard,
        currentBoard = puzzle.initialBoard,
        solution = puzzle.solution,
        selectedCell = null,
        notes = emptyMap(),
        isNotesMode = false,
        elapsedSeconds = 0,
        mistakes = 0,
        maxMistakes = 3,
        isCompleted = false,
        isGameOver = false,
        isPaused = false,
        hintsUsed = 0,
        canUndo = false,
        isLoading = false,
        isBossPuzzle = false
      )

      startTimer()
      updateGamesPlayedStat()
    }
  }

  fun startClassicGame(difficulty: Difficulty) {
    timerJob?.cancel()
    undoStack.clear()
    _currentStage.value = null
    _lastRewardSummary.value = null

    _uiState.value = _uiState.value.copy(isLoading = true)

    val puzzle = SudokuGenerator.generate(difficulty)
    val newSession = GameSessionEntity(
      id = "active_game",
      stageId = null,
      isDaily = false,
      difficulty = difficulty.name,
      initialBoard = puzzle.initialBoard,
      currentBoard = puzzle.initialBoard,
      solution = puzzle.solution,
      notes = "",
      elapsedSeconds = 0,
      mistakes = 0,
      maxMistakes = 3,
      isCompleted = false,
      isPaused = false,
      hintsUsed = 0
    )

    viewModelScope.launch {
      sudokuRepository.saveNewGame(newSession)

      _uiState.value = SudokuUiState(
        gameId = "active_game",
        stageId = null,
        isDaily = false,
        difficulty = difficulty,
        initialBoard = puzzle.initialBoard,
        currentBoard = puzzle.initialBoard,
        solution = puzzle.solution,
        selectedCell = null,
        notes = emptyMap(),
        isNotesMode = false,
        elapsedSeconds = 0,
        mistakes = 0,
        maxMistakes = 3,
        isCompleted = false,
        isGameOver = false,
        isPaused = false,
        hintsUsed = 0,
        canUndo = false,
        isLoading = false
      )

      startTimer()
      updateGamesPlayedStat()
    }
  }

  fun resumeActiveGame(game: GameSessionEntity) {
    timerJob?.cancel()
    undoStack.clear()
    _lastRewardSummary.value = null

    val diff = try {
      Difficulty.valueOf(game.difficulty)
    } catch (e: Exception) {
      Difficulty.EASY
    }

    val stage = if (game.stageId != null && !game.isDaily) {
      QuestConfig.CHAPTERS.flatMap { it.stages }.find { it.id == game.stageId }
    } else {
      null
    }
    _currentStage.value = stage

    val parsedNotes = decodeNotes(game.notes)

    _uiState.value = SudokuUiState(
      gameId = game.id,
      stageId = game.stageId,
      isDaily = game.isDaily,
      difficulty = diff,
      initialBoard = game.initialBoard,
      currentBoard = game.currentBoard,
      solution = game.solution,
      selectedCell = null,
      notes = parsedNotes,
      isNotesMode = false,
      elapsedSeconds = game.elapsedSeconds,
      mistakes = game.mistakes,
      maxMistakes = game.maxMistakes,
      isCompleted = game.isCompleted,
      isGameOver = game.mistakes >= game.maxMistakes,
      isPaused = false,
      hintsUsed = game.hintsUsed,
      canUndo = false,
      isLoading = false,
      isBossPuzzle = stage?.isBoss == true,
      bossName = stage?.bossName
    )

    startTimer()
  }

  fun selectCell(row: Int, col: Int) {
    if (_uiState.value.isCompleted || _uiState.value.isGameOver || _uiState.value.isPaused) return
    _uiState.value = _uiState.value.copy(selectedCell = Pair(row, col))
  }

  fun toggleNotesMode() {
    _uiState.value = _uiState.value.copy(isNotesMode = !_uiState.value.isNotesMode)
  }

  fun inputNumber(number: Int) {
    val state = _uiState.value
    if (state.isCompleted || state.isGameOver || state.isPaused) return
    val selected = state.selectedCell ?: return
    val index = selected.first * 9 + selected.second

    if (state.initialBoard.getOrNull(index) != '0') return

    if (state.isNotesMode) {
      val currentNotes = state.notes[index] ?: emptySet()
      val newNotes = if (currentNotes.contains(number)) {
        currentNotes - number
      } else {
        currentNotes + number
      }
      val updatedNotesMap = if (newNotes.isEmpty()) {
        state.notes - index
      } else {
        state.notes + (index to newNotes)
      }

      undoStack.add(MoveAction.NoteChange(index, currentNotes, newNotes))
      _uiState.value = state.copy(
        notes = updatedNotesMap,
        canUndo = true
      )
      scheduleAutoSave()
    } else {
      val currentChar = state.currentBoard.getOrNull(index) ?: '0'
      val inputChar = ('0' + number)

      if (currentChar == inputChar) return

      val solutionChar = state.solution.getOrNull(index) ?: '0'
      val isMistake = inputChar != solutionChar

      val newBoardChars = state.currentBoard.toCharArray()
      newBoardChars[index] = inputChar
      val newBoard = String(newBoardChars)

      val newMistakes = if (isMistake) state.mistakes + 1 else state.mistakes
      val isGameOver = newMistakes >= state.maxMistakes
      val isCompleted = !isGameOver && (newBoard == state.solution)

      val updatedNotesMap = state.notes.toMutableMap()
      updatedNotesMap.remove(index)
      if (!isMistake) {
        val row = selected.first
        val col = selected.second
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (i in 0 until 81) {
          val r = i / 9
          val c = i % 9
          if (r == row || c == col || (r in startRow until startRow + 3 && c in startCol until startCol + 3)) {
            val cellNotes = updatedNotesMap[i]
            if (cellNotes != null && cellNotes.contains(number)) {
              val filtered = cellNotes - number
              if (filtered.isEmpty()) updatedNotesMap.remove(i) else updatedNotesMap[i] = filtered
            }
          }
        }
      }

      // Check section completion feedback
      val completedSection = if (!isMistake && !isCompleted) {
        checkSectionCompleted(newBoard, state.solution, selected.first, selected.second)
      } else null

      undoStack.add(MoveAction.ValueChange(index, currentChar, inputChar))

      _uiState.value = state.copy(
        currentBoard = newBoard,
        notes = updatedNotesMap,
        mistakes = newMistakes,
        isGameOver = isGameOver,
        isCompleted = isCompleted,
        canUndo = true,
        lastCompletedSection = completedSection
      )

      if (isCompleted) {
        timerJob?.cancel()
        handleGameWon()
      } else if (isGameOver) {
        timerJob?.cancel()
        handleGameOver()
      }

      scheduleAutoSave()
    }
  }

  private fun checkSectionCompleted(currentBoard: String, solution: String, row: Int, col: Int): String? {
    // Check row
    var rowComplete = true
    for (c in 0 until 9) {
      val idx = row * 9 + c
      if (currentBoard[idx] == '0' || currentBoard[idx] != solution[idx]) {
        rowComplete = false
        break
      }
    }
    if (rowComplete) return "Linha completa!"

    // Check col
    var colComplete = true
    for (r in 0 until 9) {
      val idx = r * 9 + col
      if (currentBoard[idx] == '0' || currentBoard[idx] != solution[idx]) {
        colComplete = false
        break
      }
    }
    if (colComplete) return "Coluna completa!"

    // Check 3x3 block
    val startRow = (row / 3) * 3
    val startCol = (col / 3) * 3
    var blockComplete = true
    for (r in 0 until 3) {
      for (c in 0 until 3) {
        val idx = (startRow + r) * 9 + (startCol + c)
        if (currentBoard[idx] == '0' || currentBoard[idx] != solution[idx]) {
          blockComplete = false
          break
        }
      }
    }
    if (blockComplete) return "Quadrante 3x3 completo!"

    return null
  }

  fun eraseCell() {
    val state = _uiState.value
    if (state.isCompleted || state.isGameOver || state.isPaused) return
    val selected = state.selectedCell ?: return
    val index = selected.first * 9 + selected.second

    if (state.initialBoard.getOrNull(index) != '0') return

    val currentChar = state.currentBoard.getOrNull(index) ?: '0'
    val currentNotes = state.notes[index] ?: emptySet()

    if (currentChar != '0') {
      val newBoardChars = state.currentBoard.toCharArray()
      newBoardChars[index] = '0'
      undoStack.add(MoveAction.ValueChange(index, currentChar, '0'))
      _uiState.value = state.copy(
        currentBoard = String(newBoardChars),
        canUndo = true
      )
      scheduleAutoSave()
    } else if (currentNotes.isNotEmpty()) {
      undoStack.add(MoveAction.NoteChange(index, currentNotes, emptySet()))
      _uiState.value = state.copy(
        notes = state.notes - index,
        canUndo = true
      )
      scheduleAutoSave()
    }
  }

  fun undo() {
    val state = _uiState.value
    if (undoStack.isEmpty() || state.isCompleted || state.isGameOver) return

    val lastMove = undoStack.removeAt(undoStack.lastIndex)
    when (lastMove) {
      is MoveAction.ValueChange -> {
        val boardChars = state.currentBoard.toCharArray()
        boardChars[lastMove.index] = lastMove.oldChar
        _uiState.value = state.copy(
          currentBoard = String(boardChars),
          canUndo = undoStack.isNotEmpty()
        )
      }
      is MoveAction.NoteChange -> {
        val updatedNotes = state.notes.toMutableMap()
        if (lastMove.oldNotes.isEmpty()) {
          updatedNotes.remove(lastMove.index)
        } else {
          updatedNotes[lastMove.index] = lastMove.oldNotes
        }
        _uiState.value = state.copy(
          notes = updatedNotes,
          canUndo = undoStack.isNotEmpty()
        )
      }
    }
    scheduleAutoSave()
  }

  fun useHint() {
    val state = _uiState.value
    if (state.isCompleted || state.isGameOver || state.isPaused) return
    val selected = state.selectedCell ?: return
    val index = selected.first * 9 + selected.second

    if (state.initialBoard.getOrNull(index) != '0') return
    val correctChar = state.solution.getOrNull(index) ?: return
    if (state.currentBoard.getOrNull(index) == correctChar) return

    val newBoardChars = state.currentBoard.toCharArray()
    newBoardChars[index] = correctChar
    val newBoard = String(newBoardChars)
    val isCompleted = newBoard == state.solution

    _uiState.value = state.copy(
      currentBoard = newBoard,
      notes = state.notes - index,
      hintsUsed = state.hintsUsed + 1,
      isCompleted = isCompleted
    )

    if (isCompleted) {
      timerJob?.cancel()
      handleGameWon()
    }

    scheduleAutoSave()
  }

  fun pauseGame() {
    _uiState.value = _uiState.value.copy(isPaused = true)
    timerJob?.cancel()
    scheduleAutoSave()
  }

  fun resumeGame() {
    _uiState.value = _uiState.value.copy(isPaused = false)
    startTimer()
    scheduleAutoSave()
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000L)
        val state = _uiState.value
        if (!state.isPaused && !state.isCompleted && !state.isGameOver) {
          _uiState.value = state.copy(elapsedSeconds = state.elapsedSeconds + 1)
          if (_uiState.value.elapsedSeconds % 10 == 0) {
            scheduleAutoSave()
          }
        }
      }
    }
  }

  private fun scheduleAutoSave() {
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      delay(300L)
      val state = _uiState.value

      val encodedNotes = encodeNotes(state.notes)
      val session = GameSessionEntity(
        id = state.gameId,
        stageId = state.stageId,
        isDaily = state.isDaily,
        difficulty = state.difficulty.name,
        initialBoard = state.initialBoard,
        currentBoard = state.currentBoard,
        solution = state.solution,
        notes = encodedNotes,
        elapsedSeconds = state.elapsedSeconds,
        mistakes = state.mistakes,
        maxMistakes = state.maxMistakes,
        isCompleted = state.isCompleted,
        isPaused = state.isPaused,
        hintsUsed = state.hintsUsed
      )
      sudokuRepository.updateGame(session)
    }
  }

  private fun handleGameWon() {
    viewModelScope.launch {
      val state = _uiState.value
      val elapsed = state.elapsedSeconds

      // Star calculation:
      // 3 stars: 0 mistakes and 0 hints used
      // 2 stars: <= 1 mistake and <= 1 hint used
      // 1 star: completed
      val stars = when {
        state.mistakes == 0 && state.hintsUsed == 0 -> 3
        state.mistakes <= 1 && state.hintsUsed <= 1 -> 2
        else -> 1
      }

      if (state.stageId != null && !state.isDaily) {
        val summary = questRepository.completeStage(
          stageId = state.stageId,
          stars = stars,
          timeSeconds = elapsed,
          mistakes = state.mistakes,
          hintsUsed = state.hintsUsed
        )
        _lastRewardSummary.value = summary
      } else if (state.isDaily) {
        val summary = questRepository.completeDailyChallenge(elapsed)
        _lastRewardSummary.value = summary
      } else {
        // Classic free play: standard reward
        _lastRewardSummary.value = RewardSummary(
          xpEarned = QuestConfig.XP_MEDIUM,
          coinsEarned = QuestConfig.COINS_MEDIUM,
          stars = stars,
          didLevelUp = false,
          newLevel = playerProfile.value.level
        )
      }

      // Update Lifetime Stats
      val currentStats = _userStats.value ?: UserStatsEntity()
      val newBestEasy = if (state.difficulty == Difficulty.EASY) {
        if (currentStats.bestTimeSecondsEasy == 0) elapsed else minOf(currentStats.bestTimeSecondsEasy, elapsed)
      } else currentStats.bestTimeSecondsEasy

      val newBestMedium = if (state.difficulty == Difficulty.MEDIUM) {
        if (currentStats.bestTimeSecondsMedium == 0) elapsed else minOf(currentStats.bestTimeSecondsMedium, elapsed)
      } else currentStats.bestTimeSecondsMedium

      val newBestHard = if (state.difficulty == Difficulty.HARD) {
        if (currentStats.bestTimeSecondsHard == 0) elapsed else minOf(currentStats.bestTimeSecondsHard, elapsed)
      } else currentStats.bestTimeSecondsHard

      val newBestExpert = if (state.difficulty == Difficulty.EXPERT) {
        if (currentStats.bestTimeSecondsExpert == 0) elapsed else minOf(currentStats.bestTimeSecondsExpert, elapsed)
      } else currentStats.bestTimeSecondsExpert

      val newStreak = currentStats.currentStreak + 1
      val newBestStreak = maxOf(currentStats.bestStreak, newStreak)

      val updatedStats = currentStats.copy(
        gamesWon = currentStats.gamesWon + 1,
        bestTimeSecondsEasy = newBestEasy,
        bestTimeSecondsMedium = newBestMedium,
        bestTimeSecondsHard = newBestHard,
        bestTimeSecondsExpert = newBestExpert,
        currentStreak = newStreak,
        bestStreak = newBestStreak
      )
      sudokuRepository.saveUserStats(updatedStats)
    }
  }

  private fun handleGameOver() {
    viewModelScope.launch {
      val currentStats = _userStats.value ?: UserStatsEntity()
      val updatedStats = currentStats.copy(currentStreak = 0)
      sudokuRepository.saveUserStats(updatedStats)
    }
  }

  private fun updateGamesPlayedStat() {
    viewModelScope.launch {
      val currentStats = _userStats.value ?: UserStatsEntity()
      val updatedStats = currentStats.copy(gamesPlayed = currentStats.gamesPlayed + 1)
      sudokuRepository.saveUserStats(updatedStats)
    }
  }

  fun startNewGame(difficulty: Difficulty) = startClassicGame(difficulty)
  fun selectCharacter(characterId: String) = selectCompanion(characterId)

  fun selectCompanion(characterId: String) {
    viewModelScope.launch {
      questRepository.selectCompanion(characterId)
    }
  }

  fun claimAchievement(achievementId: String) {
    viewModelScope.launch {
      questRepository.claimAchievement(achievementId)
    }
  }

  fun dismissRewardSummary() {
    _lastRewardSummary.value = null
  }

  private fun encodeNotes(notes: Map<Int, Set<Int>>): String {
    return notes.filter { it.value.isNotEmpty() }
      .map { "${it.key}:${it.value.sorted().joinToString(",")}" }
      .joinToString(";")
  }

  private fun decodeNotes(encoded: String?): Map<Int, Set<Int>> {
    if (encoded.isNullOrBlank()) return emptyMap()
    val map = mutableMapOf<Int, Set<Int>>()
    try {
      val items = encoded.split(";")
      for (item in items) {
        val parts = item.split(":")
        if (parts.size == 2) {
          val idx = parts[0].toIntOrNull()
          val nums = parts[1].split(",").mapNotNull { it.toIntOrNull() }.toSet()
          if (idx != null && nums.isNotEmpty()) {
            map[idx] = nums
          }
        }
      }
    } catch (e: Exception) {
      // Ignore
    }
    return map
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
    autoSaveJob?.cancel()
  }
}
