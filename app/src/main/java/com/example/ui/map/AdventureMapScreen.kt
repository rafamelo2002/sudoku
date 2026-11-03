package com.example.ui.map

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StageProgressEntity
import com.example.data.model.Chapter
import com.example.data.model.QuestConfig
import com.example.data.model.Stage
import com.example.game.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureMapScreen(
  stageProgressMap: Map<String, StageProgressEntity>,
  onBack: () -> Unit,
  onSelectStage: (Stage) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  var selectedChapterIndex by remember { mutableIntStateOf(0) }
  var selectedStageForDetails by remember { mutableStateOf<Stage?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val chapters = QuestConfig.ALL_CHAPTERS
  val currentChapter = chapters.getOrElse(selectedChapterIndex) { chapters.first() }

  // Total stars in current chapter
  val chapterStars = currentChapter.stages.sumOf { stage ->
    stageProgressMap[stage.id]?.stars ?: 0
  }
  val maxChapterStars = currentChapter.stages.size * 3

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Mapa da Aventura",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Capítulo ${currentChapter.number}: ${currentChapter.name}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("map_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Voltar"
            )
          }
        },
        actions = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(end = 16.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$chapterStars / $maxChapterStars",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )

      // Chapter Tabs
      ScrollableTabRow(
        selectedTabIndex = selectedChapterIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        chapters.forEachIndexed { index, chapter ->
          Tab(
            selected = selectedChapterIndex == index,
            onClick = { selectedChapterIndex = index },
            text = {
              Text(
                text = "${chapter.icon} Cap. ${chapter.number}",
                fontWeight = if (selectedChapterIndex == index) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }

      // Chapter Header Card
      ChapterBanner(chapter = currentChapter)

      // Stages List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(currentChapter.stages) { stage ->
          val progress = stageProgressMap[stage.id]
          val isUnlocked = progress?.isUnlocked ?: (stage.id == "c1_s1")
          val isCompleted = progress?.isCompleted ?: false
          val stars = progress?.stars ?: 0
          val bestTime = progress?.bestTimeSeconds ?: 0

          StageCardItem(
            stage = stage,
            isUnlocked = isUnlocked,
            isCompleted = isCompleted,
            stars = stars,
            bestTimeSeconds = bestTime,
            onClick = {
              if (isUnlocked) {
                selectedStageForDetails = stage
              }
            }
          )
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Stage Details Sheet
  selectedStageForDetails?.let { stage ->
    val progress = stageProgressMap[stage.id]
    val stars = progress?.stars ?: 0
    val bestTime = progress?.bestTimeSeconds ?: 0

    ModalBottomSheet(
      onDismissRequest = { selectedStageForDetails = null },
      sheetState = sheetState
    ) {
      StageDetailSheetContent(
        stage = stage,
        stars = stars,
        bestTimeSeconds = bestTime,
        onStart = {
          selectedStageForDetails = null
          onSelectStage(stage)
        },
        onDismiss = { selectedStageForDetails = null }
      )
    }
  }
}

@Composable
private fun ChapterBanner(chapter: Chapter) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = chapter.icon,
        fontSize = 36.sp,
        modifier = Modifier.padding(end = 14.dp)
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = chapter.themeTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
          text = chapter.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
      }
    }
  }
}

@Composable
private fun StageCardItem(
  stage: Stage,
  isUnlocked: Boolean,
  isCompleted: Boolean,
  stars: Int,
  bestTimeSeconds: Int,
  onClick: () -> Unit
) {
  val borderColor = when {
    stage.isBoss && isUnlocked -> Color(0xFFEF4444)
    isCompleted -> Color(0xFF10B981)
    isUnlocked -> MaterialTheme.colorScheme.primary
    else -> Color.Transparent
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = isUnlocked, onClick = onClick)
      .border(
        width = if (isUnlocked) 1.5.dp else 0.dp,
        color = borderColor,
        shape = RoundedCornerShape(18.dp)
      )
      .testTag("stage_card_${stage.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        stage.isBoss -> Color(0xFF3B1D2A)
        isCompleted -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Stage Number / Boss Badge
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(
            when {
              !isUnlocked -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
              stage.isBoss -> Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF7F1D1D)))
              isCompleted -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
              else -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        if (!isUnlocked) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Bloqueado",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
          )
        } else if (stage.isBoss) {
          Text(text = "👑", fontSize = 22.sp)
        } else if (isCompleted) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Concluído",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        } else {
          Text(
            text = "${stage.number}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Stage Info
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stage.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF64748B)
          )
          if (stage.isBoss) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEF4444))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "BOSS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp
              )
            }
          }
        }

        Text(
          text = if (isUnlocked) stage.subtitle else "Conclua a fase anterior para desbloquear",
          style = MaterialTheme.typography.bodySmall,
          color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B)
        )

        if (isUnlocked && isCompleted && bestTimeSeconds > 0) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            val m = bestTimeSeconds / 60
            val s = bestTimeSeconds % 60
            Text(
              text = "Recorde: ${String.format("%02d:%02d", m, s)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Stars earned
      if (isUnlocked) {
        Column(horizontalAlignment = Alignment.End) {
          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..3) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= stars) Color(0xFFF59E0B) else Color(0xFF475569),
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "+${stage.xpReward} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6366F1),
            fontSize = 11.sp
          )
        }
      }
    }
  }
}

@Composable
private fun StageDetailSheetContent(
  stage: Stage,
  stars: Int,
  bestTimeSeconds: Int,
  onStart: () -> Unit,
  onDismiss: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
      .widthIn(max = 500.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header
    Text(
      text = if (stage.isBoss) "⚔️ ${stage.title}" else "Fase ${stage.number} • ${stage.title}",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.ExtraBold,
      textAlign = TextAlign.Center
    )

    Text(
      text = stage.subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    // Boss callout if boss
    if (stage.isBoss && stage.bossName != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "👹", fontSize = 28.sp)
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Desafio do Guardião: ${stage.bossName}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFECACA)
            )
            Text(
              text = "Sudoku desafiador com limite de 3 erros. Derrote-o para desbloquear novos companheiros!",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFFCA5A5)
            )
          }
        }
      }
    }

    // Stars info
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 16.dp)
    ) {
      for (i in 1..3) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (i <= stars) Color(0xFFF59E0B) else Color(0xFF475569),
          modifier = Modifier.size(32.dp)
        )
      }
    }

    // Rules for 3 stars
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Recompensas por Conclusão:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "⚡ Experiência: +${stage.xpReward} XP",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "🪙 Moedas: +${stage.coinReward}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFF59E0B)
          )
        }
        if (stage.unlockCompanionId != null) {
          Text(
            text = "🎁 Desbloqueia novo Herói ao vencer!",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action button
    Button(
      onClick = onStart,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("start_stage_confirm_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = if (stage.isBoss) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
      )
    ) {
      Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (stage.isBoss) "Enfrentar Guardião" else "Jogar Fase",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
