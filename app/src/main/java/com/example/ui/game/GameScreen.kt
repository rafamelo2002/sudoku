package com.example.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanionCharacter
import com.example.data.model.RewardSummary
import com.example.game.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
  uiState: SudokuUiState,
  companion: CompanionCharacter?,
  rewardSummary: RewardSummary?,
  onCellClick: (Int, Int) -> Unit,
  onNumberInput: (Int) -> Unit,
  onErase: () -> Unit,
  onUndo: () -> Unit,
  onToggleNotes: () -> Unit,
  onUseHint: () -> Unit,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onNewGame: (Difficulty) -> Unit,
  onNextStage: (() -> Unit)?,
  onBackToMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showExitConfirmDialog by remember { mutableStateOf(false) }

  BackHandler {
    if (!uiState.isCompleted && !uiState.isGameOver) {
      showExitConfirmDialog = true
    } else {
      onBackToMenu()
    }
  }

  val minutes = uiState.elapsedSeconds / 60
  val seconds = uiState.elapsedSeconds % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)

  val remainingCounts = remember(uiState.currentBoard, uiState.solution) {
    (1..9).associateWith { digit ->
      val count = uiState.currentBoard.count { it - '0' == digit }
      (9 - count).coerceAtLeast(0)
    }
  }

  val diffName = when (uiState.difficulty) {
    Difficulty.EASY -> "Fácil"
    Difficulty.MEDIUM -> "Médio"
    Difficulty.HARD -> "Difícil"
    Difficulty.EXPERT -> "Mestre"
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (uiState.isBossPuzzle) {
                Text(
                  text = "⚔️ ${uiState.bossName ?: "Boss Guardião"}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFFEF4444)
                )
              } else if (uiState.isDaily) {
                Text(
                  text = "📅 Desafio Diário",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              } else {
                Text(
                  text = "Sudoku Quest",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Text(
              text = if (uiState.isBossPuzzle) "Limite de 3 erros" else "Nível $diffName",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (!uiState.isCompleted && !uiState.isGameOver) {
                showExitConfirmDialog = true
              } else {
                onBackToMenu()
              }
            },
            modifier = Modifier.testTag("game_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Voltar"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (uiState.isPaused) onResume() else onPause()
            },
            modifier = Modifier.testTag("pause_resume_button")
          ) {
            Icon(
              imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (uiState.isPaused) "Continuar" else "Pausar",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = if (uiState.isBossPuzzle) Color(0xFF2D1115) else MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier
  ) { innerPadding ->
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .widthIn(max = 500.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {

        // BOSS / STAGE AURA BAR
        if (uiState.isBossPuzzle) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.horizontalGradient(listOf(Color(0xFF7F1D1D), Color(0xFFDC2626), Color(0xFF7F1D1D)))
              )
              .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "⚡ CONFRONTO DO GUARDIÃO ⚡",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              letterSpacing = 1.sp
            )
          }
        }

        // STATUS BAR (Vidas, Herói, Tempo)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Erros com corações
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Vidas: ",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val remainingLives = (uiState.maxMistakes - uiState.mistakes).coerceAtLeast(0)
            for (i in 1..uiState.maxMistakes) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = if (i <= remainingLives) Color(0xFFEF4444) else Color(0xFF64748B),
                modifier = Modifier.size(16.dp).padding(horizontal = 1.dp)
              )
            }
          }

          // Companion Mini Badge
          companion?.let { hero ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(text = hero.avatarEmoji, fontSize = 12.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = hero.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Timer
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = timeFormatted,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Section Completion Banner Animation
        AnimatedVisibility(
          visible = uiState.lastCompletedSection != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF10B981))
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = uiState.lastCompletedSection ?: "",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        // SUDOKU 9x9 BOARD
        SudokuBoard(
          initialBoard = uiState.initialBoard,
          currentBoard = uiState.currentBoard,
          solution = uiState.solution,
          selectedCell = uiState.selectedCell,
          notes = uiState.notes,
          isPaused = uiState.isPaused,
          lastCompletedSection = uiState.lastCompletedSection,
          onCellClick = onCellClick
        )

        // ACTION BUTTONS (Desfazer, Apagar, Lápis/Notas, Dica)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          ActionButtonItem(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Desfazer",
            tag = "undo_button",
            enabled = uiState.canUndo,
            onClick = onUndo
          )
          ActionButtonItem(
            icon = Icons.Default.Clear,
            label = "Apagar",
            tag = "erase_button",
            enabled = uiState.selectedCell != null,
            onClick = onErase
          )
          ActionButtonItem(
            icon = Icons.Default.Edit,
            label = "Notas",
            tag = "notes_button",
            isActive = uiState.isNotesMode,
            badge = if (uiState.isNotesMode) "ON" else null,
            onClick = onToggleNotes
          )
          ActionButtonItem(
            icon = Icons.Default.Lightbulb,
            label = "Dica",
            tag = "hint_button",
            badge = if (uiState.hintsUsed > 0) "${uiState.hintsUsed}" else null,
            onClick = onUseHint
          )
        }

        // NUMBER KEYPAD (1 a 9)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          for (num in 1..9) {
            val remaining = remainingCounts[num] ?: 0
            val isCompleted = remaining == 0
            NumberKeypadButton(
              number = num,
              remaining = remaining,
              isCompleted = isCompleted,
              enabled = !isCompleted && !uiState.isPaused && !uiState.isGameOver,
              onClick = { onNumberInput(num) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // PAUSE OVERLAY
  if (uiState.isPaused) {
    AlertDialog(
      onDismissRequest = onResume,
      title = {
        Text("Jogo Pausado", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
      },
      text = {
        Text("O cronômetro está parado. Pronto para continuar a aventura?")
      },
      confirmButton = {
        Button(
          onClick = onResume,
          modifier = Modifier.testTag("dialog_resume_button")
        ) {
          Text("Continuar")
        }
      },
      dismissButton = {
        TextButton(onClick = onBackToMenu) {
          Text("Voltar ao Menu")
        }
      }
    )
  }

  // EXIT CONFIRMATION DIALOG
  if (showExitConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showExitConfirmDialog = false },
      title = { Text("Deseja sair?") },
      text = { Text("Seu progresso nesta partida está salvo automaticamente e você poderá continuar quando quiser.") },
      confirmButton = {
        Button(
          onClick = {
            showExitConfirmDialog = false
            onBackToMenu()
          }
        ) {
          Text("Sair")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitConfirmDialog = false }) {
          Text("Ficar")
        }
      }
    )
  }

  // GAME OVER DIALOG
  if (uiState.isGameOver) {
    AlertDialog(
      onDismissRequest = { },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Fim de Jogo!", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text("Você cometeu ${uiState.maxMistakes} erros nesta tentativa.")
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Não desanime! Toda derrota é um passo em direção à maestria.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { onNewGame(uiState.difficulty) },
          modifier = Modifier.testTag("retry_game_button")
        ) {
          Text("Tentar Novamente")
        }
      },
      dismissButton = {
        TextButton(onClick = onBackToMenu) {
          Text("Voltar ao Mapa")
        }
      }
    )
  }

  // JRPG QUEST VICTORY DIALOG
  if (uiState.isCompleted) {
    QuestVictoryDialog(
      uiState = uiState,
      timeFormatted = timeFormatted,
      companion = companion,
      reward = rewardSummary,
      onNextStage = onNextStage,
      onBackToMenu = onBackToMenu
    )
  }
}

@Composable
private fun QuestVictoryDialog(
  uiState: SudokuUiState,
  timeFormatted: String,
  companion: CompanionCharacter?,
  reward: RewardSummary?,
  onNextStage: (() -> Unit)?,
  onBackToMenu: () -> Unit
) {
  val stars = reward?.stars ?: if (uiState.mistakes == 0 && uiState.hintsUsed == 0) 3 else if (uiState.mistakes <= 1 && uiState.hintsUsed <= 2) 2 else 1

  AlertDialog(
    onDismissRequest = { },
    title = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF6366F1), Color(0xFF312E81)))),
          contentAlignment = Alignment.Center
        ) {
          Text(text = companion?.avatarEmoji ?: "✨", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (uiState.isBossPuzzle) "Guardião Derrotado!" else "Vitória Conquistada!",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = if (uiState.isBossPuzzle) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )

        Row(
          modifier = Modifier.padding(top = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (i <= stars) Color(0xFFF59E0B) else Color(0xFF475569),
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        companion?.let { hero ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Text(
              text = "\"${hero.cheerQuote}\"",
              style = MaterialTheme.typography.bodySmall,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(10.dp)
            )
          }
        }

        if (reward?.unlockedHeroName != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF065F46))
              .padding(10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "🎉 Novo Companheiro Desbloqueado: ${reward.unlockedHeroName}!",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFA7F3D0),
              textAlign = TextAlign.Center
            )
          }
        }

        if (reward?.leveledUp == true) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "⭐ LEVEL UP! Você alcançou o Nível ${reward.newLevel}! ⭐",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.Black
            )
          }
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Tempo:", style = MaterialTheme.typography.bodySmall)
              Text(timeFormatted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Erros cometidos:", style = MaterialTheme.typography.bodySmall)
              Text("${uiState.mistakes}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Dicas usadas:", style = MaterialTheme.typography.bodySmall)
              Text("${uiState.hintsUsed}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("⚡ Experiência:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
              Text("+${reward?.xpEarned ?: 50} XP", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6366F1))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("🪙 Moedas:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
              Text("+${reward?.coinsEarned ?: 10}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodySmall, color = Color(0xFFF59E0B))
            }
          }
        }
      }
    },
    confirmButton = {
      if (onNextStage != null) {
        Button(
          onClick = onNextStage,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("victory_next_stage_button")
        ) {
          Text("Próxima Fase", fontWeight = FontWeight.Bold)
        }
      } else {
        Button(
          onClick = onBackToMenu,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("victory_finish_button")
        ) {
          Text("Continuar", fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onBackToMenu) {
        Text("Voltar ao Menu")
      }
    }
  )
}

@Composable
private fun ActionButtonItem(
  icon: ImageVector,
  label: String,
  tag: String,
  isActive: Boolean = false,
  enabled: Boolean = true,
  badge: String? = null,
  onClick: () -> Unit
) {
  val containerColor = when {
    isActive -> MaterialTheme.colorScheme.primary
    enabled -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  }
  val contentColor = when {
    isActive -> MaterialTheme.colorScheme.onPrimary
    enabled -> MaterialTheme.colorScheme.onSurface
    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .background(containerColor)
        .testTag(tag),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(24.dp)
      )

      if (badge != null) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = badge,
            color = MaterialTheme.colorScheme.onError,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
    )
  }
}

@Composable
private fun NumberKeypadButton(
  number: Int,
  remaining: Int,
  isCompleted: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val bgColor = when {
    isCompleted -> if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFE2E8F0).copy(alpha = 0.4f)
    isDark -> Color(0xFF1E293B)
    else -> Color(0xFFF1F5F9)
  }

  Card(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .height(60.dp)
      .testTag("num_pad_$number"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = bgColor,
      disabledContainerColor = bgColor
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 1.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      if (isCompleted) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Número $number completo",
          tint = Color(0xFF10B981),
          modifier = Modifier.size(20.dp)
        )
      } else {
        Text(
          text = number.toString(),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Text(
          text = if (remaining > 0) "$remaining" else "",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
