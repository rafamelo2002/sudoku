package com.example.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SudokuErrorBg
import com.example.ui.theme.SudokuErrorText
import com.example.ui.theme.SudokuNoteColor
import com.example.ui.theme.SudokuNoteColorDark

@Composable
fun SudokuBoard(
  initialBoard: String,
  currentBoard: String,
  solution: String,
  selectedCell: Pair<Int, Int>?,
  notes: Map<Int, Set<Int>>,
  isPaused: Boolean,
  lastCompletedSection: String? = null,
  onCellClick: (Int, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()

  val thickLineColor = if (isDark) Color(0xFF818CF8) else Color(0xFF312E81)
  val thinLineColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

  val selectedBg = if (isDark) Color(0xFF3730A3) else Color(0xFFC7D2FE)
  val sameNumBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF)
  val relatedBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFFF8FAFC)

  val selectedNumberChar = selectedCell?.let { (r, c) ->
    val char = currentBoard.getOrNull(r * 9 + c) ?: '0'
    if (char != '0') char else null
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(3.dp, thickLineColor, RoundedCornerShape(16.dp))
  ) {
    if (isPaused) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Aventura Pausada",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      return@BoxWithConstraints
    }

    // Grid of cells
    Column(modifier = Modifier.fillMaxSize()) {
      for (row in 0 until 9) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          for (col in 0 until 9) {
            val index = row * 9 + col
            val isGiven = initialBoard.getOrNull(index) != '0'
            val charVal = currentBoard.getOrNull(index) ?: '0'
            val solutionChar = solution.getOrNull(index) ?: '0'
            val isSelected = selectedCell?.first == row && selectedCell.second == col
            val isSameNumber = selectedNumberChar != null && charVal == selectedNumberChar
            val isRelated = selectedCell != null && (
              selectedCell.first == row ||
                selectedCell.second == col ||
                ((selectedCell.first / 3 == row / 3) && (selectedCell.second / 3 == col / 3))
              )
            val isError = !isGiven && charVal != '0' && charVal != solutionChar

            val cellBg = when {
              isError -> SudokuErrorBg
              isSelected -> selectedBg
              isSameNumber -> sameNumBg
              isRelated -> relatedBg
              else -> Color.Transparent
            }

            val cellNotes = notes[index] ?: emptySet()

            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(cellBg)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  onCellClick(row, col)
                }
                .semantics {
                  contentDescription = "Linha ${row + 1}, Coluna ${col + 1}, " +
                    if (charVal != '0') "número $charVal" else "célula vazia"
                }
                .testTag("cell_${row}_${col}"),
              contentAlignment = Alignment.Center
            ) {
              if (charVal != '0') {
                val textColor = when {
                  isError -> SudokuErrorText
                  isGiven -> MaterialTheme.colorScheme.onSurface
                  else -> MaterialTheme.colorScheme.primary
                }
                val fontWeight = if (isGiven) FontWeight.ExtraBold else FontWeight.Bold
                Text(
                  text = charVal.toString(),
                  fontSize = 20.sp,
                  fontWeight = fontWeight,
                  color = textColor,
                  textAlign = TextAlign.Center
                )
              } else if (cellNotes.isNotEmpty()) {
                PencilNotesGrid(cellNotes = cellNotes, isDark = isDark)
              }
            }
          }
        }
      }
    }

    // Grid divider lines drawn cleanly over cells
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cellWidth = size.width / 9f
      val cellHeight = size.height / 9f

      for (i in 1 until 9) {
        val isSubgridDivider = (i % 3 == 0)
        val strokeWidth = if (isSubgridDivider) 2.5.dp.toPx() else 0.8.dp.toPx()
        val color = if (isSubgridDivider) thickLineColor else thinLineColor

        // Vertical lines
        val x = i * cellWidth
        drawLine(
          color = color,
          start = Offset(x, 0f),
          end = Offset(x, size.height),
          strokeWidth = strokeWidth
        )

        // Horizontal lines
        val y = i * cellHeight
        drawLine(
          color = color,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = strokeWidth
        )
      }
    }

    // Completion feedback banner over board
    AnimatedVisibility(
      visible = lastCompletedSection != null,
      enter = fadeIn(tween(200)) + scaleIn(tween(250)),
      exit = fadeOut(tween(250)) + scaleOut(tween(200)),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
    ) {
      if (lastCompletedSection != null) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF10B981))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = "✨ $lastCompletedSection",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun PencilNotesGrid(cellNotes: Set<Int>, isDark: Boolean) {
  val noteColor = if (isDark) SudokuNoteColorDark else SudokuNoteColor

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(1.5.dp),
    verticalArrangement = Arrangement.SpaceEvenly
  ) {
    for (r in 0 until 3) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        for (c in 0 until 3) {
          val num = r * 3 + c + 1
          val hasNote = cellNotes.contains(num)
          Text(
            text = if (hasNote) num.toString() else " ",
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            color = noteColor,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
