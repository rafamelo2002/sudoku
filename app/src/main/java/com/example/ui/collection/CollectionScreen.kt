package com.example.ui.collection

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.AchievementProgressEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.AchievementDef
import com.example.data.model.CharacterRarity
import com.example.data.model.CompanionCharacter
import com.example.data.model.QuestConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
  playerProfile: PlayerProfileEntity,
  unlockedCharacters: Set<String>,
  achievementsMap: Map<String, AchievementProgressEntity>,
  onSelectCharacter: (String) -> Unit,
  onClaimAchievement: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Heróis, 1: Conquistas

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopAppBar(
        title = {
          Text(
            text = "Coleção & Conquistas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("collection_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Voltar"
            )
          }
        },
        actions = {
          // Coins and Level pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(end = 16.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(text = "🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${playerProfile.coins}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Nv. ${playerProfile.level}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Heróis (${unlockedCharacters.size}/${QuestConfig.ALL_CHARACTERS.size})")
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              val claimedCount = achievementsMap.values.count { it.isClaimed }
              Text("Conquistas ($claimedCount/${QuestConfig.ALL_ACHIEVEMENTS.size})")
            }
          }
        )
      }

      Box(modifier = Modifier.weight(1f)) {
        if (selectedTab == 0) {
          CompanionsTab(
            allCharacters = QuestConfig.ALL_CHARACTERS,
            selectedCharacterId = playerProfile.selectedCharacterId,
            unlockedCharacters = unlockedCharacters,
            onSelect = onSelectCharacter
          )
        } else {
          AchievementsTab(
            allAchievements = QuestConfig.ALL_ACHIEVEMENTS,
            progressMap = achievementsMap,
            onClaim = onClaimAchievement
          )
        }
      }
    }
  }
}

@Composable
private fun CompanionsTab(
  allCharacters: List<CompanionCharacter>,
  selectedCharacterId: String,
  unlockedCharacters: Set<String>,
  onSelect: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    items(allCharacters) { character ->
      val isUnlocked = unlockedCharacters.contains(character.id)
      val isEquipped = selectedCharacterId == character.id

      CharacterCardItem(
        character = character,
        isUnlocked = isUnlocked,
        isEquipped = isEquipped,
        onEquip = { onSelect(character.id) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun CharacterCardItem(
  character: CompanionCharacter,
  isUnlocked: Boolean,
  isEquipped: Boolean,
  onEquip: () -> Unit
) {
  val rarityColor = when (character.rarity) {
    CharacterRarity.COMUM -> Color(0xFF94A3B8)
    CharacterRarity.RARO -> Color(0xFF38BDF8)
    CharacterRarity.EPICO -> Color(0xFFA855F7)
    CharacterRarity.LENDARIO -> Color(0xFFF59E0B)
  }

  val rarityLabel = when (character.rarity) {
    CharacterRarity.COMUM -> "Comum"
    CharacterRarity.RARO -> "Raro"
    CharacterRarity.EPICO -> "Épico"
    CharacterRarity.LENDARIO -> "Lendário"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = if (isEquipped) 2.dp else 1.dp,
        color = if (isEquipped) MaterialTheme.colorScheme.primary else rarityColor.copy(alpha = 0.4f),
        shape = RoundedCornerShape(20.dp)
      ),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
      else MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Character Emoji Avatar
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
              if (isUnlocked) Brush.radialGradient(listOf(rarityColor.copy(alpha = 0.5f), Color(0xFF1E293B)))
              else Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF0F172A)))
            )
            .border(2.dp, rarityColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          if (isUnlocked) {
            Text(text = character.avatarEmoji, fontSize = 32.sp)
          } else {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Bloqueado",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = character.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(rarityColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = rarityLabel,
                style = MaterialTheme.typography.labelSmall,
                color = rarityColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }

          Text(
            text = character.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )

          Text(
            text = if (isUnlocked) "\"${character.cheerQuote}\"" else "Desbloqueie concluindo os desafios de aventura",
            style = MaterialTheme.typography.bodySmall,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B),
            fontStyle = if (isUnlocked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = character.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Equip action button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isUnlocked) {
          Text(
            text = "🔒 Bloqueado no Modo Aventura",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
          )
        } else if (isEquipped) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primary)
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Em Uso",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          Button(
            onClick = onEquip,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("equip_character_${character.id}")
          ) {
            Text("Equipar", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun AchievementsTab(
  allAchievements: List<AchievementDef>,
  progressMap: Map<String, AchievementProgressEntity>,
  onClaim: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    items(allAchievements) { ach ->
      val progress = progressMap[ach.id]
      val currentProgress = progress?.currentProgress ?: 0
      val isClaimed = progress?.isClaimed ?: false
      val isCompleted = currentProgress >= ach.targetCount

      AchievementCardItem(
        achievement = ach,
        currentProgress = currentProgress,
        isCompleted = isCompleted,
        isClaimed = isClaimed,
        onClaim = { onClaim(ach.id) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun AchievementCardItem(
  achievement: AchievementDef,
  currentProgress: Int,
  isCompleted: Boolean,
  isClaimed: Boolean,
  onClaim: () -> Unit
) {
  val progressFraction = (currentProgress.toFloat() / achievement.targetCount.toFloat()).coerceIn(0f, 1f)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isClaimed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
      }
    ),
    border = if (isCompleted && !isClaimed) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Icon
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              when {
                isClaimed -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                isCompleted -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                else -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(text = achievement.icon, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = achievement.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = achievement.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar and rewards
      LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "⚡ +${achievement.xpReward} XP",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6366F1),
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "🪙 +${achievement.coinReward}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF59E0B),
            fontWeight = FontWeight.Bold
          )
        }

        if (isClaimed) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Resgatado",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF10B981),
              fontWeight = FontWeight.Bold
            )
          }
        } else if (isCompleted) {
          Button(
            onClick = onClaim,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            modifier = Modifier.testTag("claim_achievement_${achievement.id}")
          ) {
            Text("Resgatar", fontWeight = FontWeight.Bold, color = Color.Black)
          }
        } else {
          Text(
            text = "$currentProgress / ${achievement.targetCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
