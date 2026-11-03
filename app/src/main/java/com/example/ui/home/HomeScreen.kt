package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameSessionEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.UserStatsEntity
import com.example.data.model.CompanionCharacter
import com.example.data.model.QuestConfig
import com.example.game.Difficulty
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  playerProfile: PlayerProfileEntity,
  companion: CompanionCharacter,
  activeGame: GameSessionEntity?,
  userStats: UserStatsEntity?,
  isLoading: Boolean,
  onContinueAdventure: () -> Unit,
  onOpenMap: () -> Unit,
  onOpenDaily: () -> Unit,
  onOpenCollection: () -> Unit,
  onStartClassicGame: (Difficulty) -> Unit,
  onViewStats: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showRulesSheet by remember { mutableStateOf(false) }
  var showClassicSelectorSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  val greeting = when (hour) {
    in 5..11 -> "Bom dia"
    in 12..17 -> "Boa tarde"
    else -> "Boa noite"
  }

  val xpForNext = QuestConfig.getXpForNextLevel(playerProfile.level)
  val xpProgress = (playerProfile.currentXp.toFloat() / xpForNext.toFloat()).coerceIn(0f, 1f)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Sudoku Quest",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Resolva. Evolua. Conquiste.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        actions = {
          IconButton(
            onClick = { showRulesSheet = true },
            modifier = Modifier.testTag("how_to_play_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.HelpOutline,
              contentDescription = "Como Jogar",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(
            onClick = onViewStats,
            modifier = Modifier.testTag("top_stats_button")
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = "Ver estatísticas",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
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
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .widthIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {

        // 1. ANIME HERO STATUS CARD
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(62.dp)
                  .clip(CircleShape)
                  .background(Brush.radialGradient(listOf(Color(0xFF6366F1), Color(0xFF312E81))))
                  .border(2.dp, Color(0xFF818CF8), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = companion.avatarEmoji, fontSize = 32.sp)
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "$greeting, Aventureiro!",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                Text(
                  text = "${companion.name} • ${companion.title}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFC7D2FE),
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "\"${companion.cheerQuote}\"",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFA5B4FC),
                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level & XP Bar
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Nível ${playerProfile.level}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFDE68A)
              )
              Text(
                text = "${playerProfile.currentXp} / $xpForNext XP",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE0E7FF),
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
              progress = { xpProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = Color(0xFFF59E0B),
              trackColor = Color(0xFF312E81)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Coins & Daily Streak
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFFEF3C7).copy(alpha = 0.15f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(text = "🪙", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${playerProfile.coins} moedas",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFFDE68A),
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFFEE2E2).copy(alpha = 0.15f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocalFireDepartment,
                  contentDescription = null,
                  tint = Color(0xFFF97316),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Sequência: ${playerProfile.dailyStreak} dias",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFFCA5A5),
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 2. PRIMARY ACTION: CONTINUAR AVENTURA
        Button(
          onClick = onContinueAdventure,
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("continue_adventure_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (activeGame != null && !activeGame.isCompleted) "Continuar Partida Salva" else "Continuar Aventura",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
          )
        }

        // 3. MAIN NAVIGATION MENU TILES
        Text(
          text = "Modos de Jogo & Explorar",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          MenuQuestCard(
            title = "Mapa de Fases",
            subtitle = "4 Capítulos • 20 Fases",
            badge = "Aventura",
            icon = "🗺️",
            borderColor = Color(0xFF6366F1),
            modifier = Modifier.weight(1f),
            tag = "open_map_card",
            onClick = onOpenMap
          )

          MenuQuestCard(
            title = "Desafio Diário",
            subtitle = "Bônus e ofensiva diária",
            badge = "Hoje",
            icon = "📅",
            borderColor = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f),
            tag = "open_daily_card",
            onClick = onOpenDaily
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          MenuQuestCard(
            title = "Coleção & Heróis",
            subtitle = "Companheiros e Conquistas",
            badge = "Evolução",
            icon = "👥",
            borderColor = Color(0xFFA855F7),
            modifier = Modifier.weight(1f),
            tag = "open_collection_card",
            onClick = onOpenCollection
          )

          MenuQuestCard(
            title = "Modo Clássico",
            subtitle = "4 dificuldades livres",
            badge = "Treino",
            icon = "🧩",
            borderColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f),
            tag = "open_classic_card",
            onClick = { showClassicSelectorSheet = true }
          )
        }

        // 4. ACTIVE GAME NOTIFIER BANNER
        if (activeGame != null && !activeGame.isCompleted) {
          ActiveGameResumeCard(
            activeGame = activeGame,
            onResume = onContinueAdventure
          )
        }

        // 5. HOW TO PLAY FOOTER BANNER
        Card(
          onClick = { showRulesSheet = true },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Como Jogar Sudoku Quest",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Regras básicas, dicas e poderes dos guardiões",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // CLASSIC DIFFICULTY MODAL
  if (showClassicSelectorSheet) {
    ModalBottomSheet(
      onDismissRequest = { showClassicSelectorSheet = false },
      sheetState = sheetState
    ) {
      ClassicDifficultySheetContent(
        onSelectDifficulty = { diff ->
          showClassicSelectorSheet = false
          onStartClassicGame(diff)
        }
      )
    }
  }

  // RULES MODAL
  if (showRulesSheet) {
    ModalBottomSheet(
      onDismissRequest = { showRulesSheet = false },
      sheetState = sheetState
    ) {
      HowToPlaySheetContent(onClose = { showRulesSheet = false })
    }
  }
}

@Composable
private fun MenuQuestCard(
  title: String,
  subtitle: String,
  badge: String,
  icon: String,
  borderColor: Color,
  modifier: Modifier = Modifier,
  tag: String,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = modifier
      .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
      .testTag(tag),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = icon, fontSize = 28.sp)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(borderColor.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = borderColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        }
      }

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
private fun ActiveGameResumeCard(
  activeGame: GameSessionEntity,
  onResume: () -> Unit
) {
  val minutes = activeGame.elapsedSeconds / 60
  val seconds = activeGame.elapsedSeconds % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)
  val filledCount = activeGame.currentBoard.count { it != '0' }
  val progress = (filledCount / 81f * 100).toInt()

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Partida Salva em Andamento",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$progress% concluído • Tempo: $timeFormatted",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Button(
        onClick = onResume,
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Retomar")
      }
    }
  }
}

@Composable
private fun ClassicDifficultySheetContent(
  onSelectDifficulty: (Difficulty) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(24.dp)
      .widthIn(max = 500.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "Modo Clássico",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Escolha a dificuldade desejada para treinar suas habilidades:",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    ClassicDifficultyRow(
      title = "Fácil",
      description = "38 números iniciais • Relaxante",
      color = Color(0xFF10B981),
      onClick = { onSelectDifficulty(Difficulty.EASY) }
    )
    ClassicDifficultyRow(
      title = "Médio",
      description = "32 números iniciais • Equilibrado",
      color = Color(0xFF3B82F6),
      onClick = { onSelectDifficulty(Difficulty.MEDIUM) }
    )
    ClassicDifficultyRow(
      title = "Difícil",
      description = "28 números iniciais • Estratégico",
      color = Color(0xFFF59E0B),
      onClick = { onSelectDifficulty(Difficulty.HARD) }
    )
    ClassicDifficultyRow(
      title = "Mestre / Expert",
      description = "24 números iniciais • Desafio máximo",
      color = Color(0xFFEF4444),
      onClick = { onSelectDifficulty(Difficulty.EXPERT) }
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun ClassicDifficultyRow(
  title: String,
  description: String,
  color: Color,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(12.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun HowToPlaySheetContent(onClose: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(24.dp)
      .widthIn(max = 600.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Como Jogar Sudoku Quest",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold
    )

    Text(
      text = "Sudoku Quest une o raciocínio clássico do Sudoku com a progressão de uma aventura épica!",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    RuleItem(
      number = "1",
      title = "Linhas e Colunas",
      description = "Cada uma das 9 linhas e 9 colunas deve conter os números de 1 a 9 sem repetições."
    )

    RuleItem(
      number = "2",
      title = "Regiões 3x3",
      description = "Cada um dos 9 quadrantes menores deve conter todos os dígitos de 1 a 9 sem repetir."
    )

    RuleItem(
      number = "⚔️",
      title = "Modo Aventura & Guardiões",
      description = "Avance pelos 4 capítulos e derrote os Bosses no final de cada capítulo para desbloquear novos heróis e recompensas raras!"
    )

    RuleItem(
      number = "⭐",
      title = "Estrelas e Maestria",
      description = "3 estrelas: Vença sem errar e sem dicas. 2 estrelas: Poucas ajudas. 1 estrela: Concluído."
    )

    RuleItem(
      number = "💡",
      title = "Dica: Anotações a Lápis",
      description = "Ative o botão Notas (ícone de lápis) para rascunhar candidatos possíveis em células incertas."
    )

    Button(
      onClick = onClose,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      shape = RoundedCornerShape(12.dp)
    ) {
      Text("Entendi, vamos à jornada!", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun RuleItem(
  number: String,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
