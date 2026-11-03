package com.example.game

import kotlin.random.Random

enum class Difficulty(val titlePt: String, val clueCount: Int) {
  EASY("Fácil", 38),
  MEDIUM("Médio", 32),
  HARD("Difícil", 28),
  EXPERT("Especialista", 24)
}

data class PuzzleData(
  val initialBoard: String,
  val solution: String,
  val difficulty: Difficulty
)

object SudokuGenerator {

  fun generate(difficulty: Difficulty, random: Random = Random): PuzzleData {
    val solutionGrid = IntArray(81)
    fillGrid(solutionGrid, random)
    val solutionStr = solutionGrid.joinToString("")

    val puzzleGrid = solutionGrid.clone()
    val cellsToRemove = 81 - difficulty.clueCount
    val positions = (0 until 81).shuffled(random).toMutableList()

    var removed = 0
    for (pos in positions) {
      if (removed >= cellsToRemove) break
      val temp = puzzleGrid[pos]
      puzzleGrid[pos] = 0

      // Verify that the puzzle still has a unique solution
      val copy = puzzleGrid.clone()
      if (countSolutions(copy, 0, 2) != 1) {
        puzzleGrid[pos] = temp // Revert if not unique
      } else {
        removed++
      }
    }

    val initialStr = puzzleGrid.joinToString("")
    return PuzzleData(
      initialBoard = initialStr,
      solution = solutionStr,
      difficulty = difficulty
    )
  }

  private fun fillGrid(grid: IntArray, random: Random): Boolean {
    for (i in 0 until 81) {
      if (grid[i] == 0) {
        val row = i / 9
        val col = i % 9
        val numbers = (1..9).shuffled(random)
        for (num in numbers) {
          if (isValid(grid, row, col, num)) {
            grid[i] = num
            if (fillGrid(grid, random)) return true
            grid[i] = 0
          }
        }
        return false
      }
    }
    return true
  }

  private fun countSolutions(grid: IntArray, count: Int, limit: Int): Int {
    var solutions = count
    var firstEmpty = -1
    for (i in 0 until 81) {
      if (grid[i] == 0) {
        firstEmpty = i
        break
      }
    }
    if (firstEmpty == -1) {
      return solutions + 1
    }

    val row = firstEmpty / 9
    val col = firstEmpty % 9
    for (num in 1..9) {
      if (isValid(grid, row, col, num)) {
        grid[firstEmpty] = num
        solutions = countSolutions(grid, solutions, limit)
        grid[firstEmpty] = 0
        if (solutions >= limit) return solutions
      }
    }
    return solutions
  }

  fun isValid(grid: IntArray, row: Int, col: Int, num: Int): Boolean {
    // Check row
    for (c in 0 until 9) {
      if (grid[row * 9 + c] == num) return false
    }
    // Check col
    for (r in 0 until 9) {
      if (grid[r * 9 + col] == num) return false
    }
    // Check 3x3 box
    val startRow = (row / 3) * 3
    val startCol = (col / 3) * 3
    for (r in 0 until 3) {
      for (c in 0 until 3) {
        if (grid[(startRow + r) * 9 + (startCol + c)] == num) return false
      }
    }
    return true
  }

  fun isBoardComplete(board: String, solution: String): Boolean {
    if (board.length != 81 || solution.length != 81) return false
    return board == solution
  }
}
