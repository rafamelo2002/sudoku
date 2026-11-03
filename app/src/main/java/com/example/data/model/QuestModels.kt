package com.example.data.model

import com.example.game.Difficulty

enum class CharacterRarity(val label: String, val colorHex: Long) {
  COMUM("Comum", 0xFF94A3B8),
  RARO("Raro", 0xFF3B82F6),
  EPICO("Épico", 0xFFA855F7),
  LENDARIO("Lendário", 0xFFF59E0B);

  companion object {
    val COMMON: CharacterRarity get() = COMUM
    val RARE: CharacterRarity get() = RARO
    val EPIC: CharacterRarity get() = EPICO
    val LEGENDARY: CharacterRarity get() = LENDARIO
  }
}

data class CompanionCharacter(
  val id: String,
  val name: String,
  val title: String,
  val rarity: CharacterRarity,
  val description: String,
  val quoteGreeting: String,
  val quoteVictory: String,
  val quoteCheer: String,
  val unlockRequirement: String,
  val accentColorHex: Long,
  val avatarResId: Int? = null,
  val avatarEmoji: String = when (id) {
    "hikari" -> "🌸"
    "ren" -> "🍃"
    "kael" -> "⚡"
    "aria" -> "✨"
    else -> "🧙"
  }
) {
  val cheerQuote: String get() = quoteCheer
}

data class Stage(
  val id: String,
  val chapterId: Int,
  val stageNumber: Int,
  val name: String,
  val difficulty: Difficulty,
  val isBoss: Boolean = false,
  val bossName: String? = null,
  val bossDescription: String? = null,
  val xpReward: Int,
  val coinsReward: Int,
  val specialReward: String? = null
) {
  val number: Int get() = stageNumber
  val title: String get() = name
  val subtitle: String get() = if (isBoss) "Boss: ${bossName ?: name}" else "Fase $stageNumber"
  val coinReward: Int get() = coinsReward
  val unlockCompanionId: String? get() = when (id) {
    "c1_s5" -> "ren"
    "c2_s5" -> "kael"
    "c3_s5" -> "aria"
    else -> null
  }
}

data class Chapter(
  val id: Int,
  val name: String,
  val subtitle: String,
  val description: String,
  val iconEmoji: String,
  val themeColorHex: Long,
  val stages: List<Stage>
) {
  val number: Int get() = id
  val icon: String get() = iconEmoji
  val themeTitle: String get() = "$iconEmoji $name - $subtitle"
}

data class AchievementDef(
  val id: String,
  val title: String,
  val description: String,
  val iconEmoji: String,
  val target: Int,
  val rewardXp: Int,
  val rewardCoins: Int
) {
  val icon: String get() = iconEmoji
  val targetCount: Int get() = target
  val xpReward: Int get() = rewardXp
  val coinReward: Int get() = rewardCoins
}

data class RewardSummary(
  val xpEarned: Int,
  val coinsEarned: Int,
  val stars: Int,
  val didLevelUp: Boolean,
  val newLevel: Int,
  val unlockedNextStageId: String? = null,
  val newlyUnlockedCharacter: CompanionCharacter? = null
) {
  val leveledUp: Boolean get() = didLevelUp
  val unlockedHeroName: String? get() = newlyUnlockedCharacter?.name
}

object QuestConfig {
  val XP_EASY = 50
  val COINS_EASY = 10
  val XP_MEDIUM = 100
  val COINS_MEDIUM = 20
  val XP_HARD = 150
  val COINS_HARD = 35
  val XP_EXPERT = 200
  val COINS_EXPERT = 50
  val XP_BOSS = 250
  val COINS_BOSS = 60
  val XP_DAILY = 120
  val COINS_DAILY = 30

  fun xpForLevel(level: Int): Int = level * 100
  fun getXpForNextLevel(level: Int): Int = xpForLevel(level)

  val ALL_CHARACTERS = listOf(
    CompanionCharacter(
      id = "hikari",
      name = "Hikari",
      title = "A Mestra da Luz",
      rarity = CharacterRarity.COMUM,
      description = "Uma jovem sábia com afinidade mágica pelas runas sagradas dos números.",
      quoteGreeting = "Olá, aventureiro! Que a luz das runas guie sua mente hoje.",
      quoteVictory = "Incrível! Sua lógica resplandece como as estrelas!",
      quoteCheer = "Concentre-se nos padrões, você consegue!",
      unlockRequirement = "Companheira inicial",
      accentColorHex = 0xFF6366F1
    ),
    CompanionCharacter(
      id = "ren",
      name = "Ren",
      title = "Guardião dos Ventos",
      rarity = CharacterRarity.RARO,
      description = "Espadachim veloz que calcula cada jogada como o sopro de uma tempestade.",
      quoteGreeting = "A brisa da aventura está forte. Pronto para o próximo enigma?",
      quoteVictory = "Um corte preciso e sem erros! Vitória gloriosa!",
      quoteCheer = "Respire fundo e observe as colunas abertas.",
      unlockRequirement = "Complete o Capítulo 1 (Vila Sakura)",
      accentColorHex = 0xFF10B981
    ),
    CompanionCharacter(
      id = "kael",
      name = "Kael",
      title = "Alquimista Neon",
      rarity = CharacterRarity.EPICO,
      description = "Mestre da cibernética que transforma números em frequências luminosas.",
      quoteGreeting = "Sistemas sincronizados. Vamos decifrar a matriz numérica.",
      quoteVictory = "Algoritmo resolvido com 100% de precisão!",
      quoteCheer = "Isole os quadrantes com menor entropia.",
      unlockRequirement = "Complete o Capítulo 2 (Floresta Espiritual)",
      accentColorHex = 0xFFEC4899
    ),
    CompanionCharacter(
      id = "aria",
      name = "Aria",
      title = "Sacerdotisa Celestial",
      rarity = CharacterRarity.LENDARIO,
      description = "Entidade cósmica que guarda os segredos matemáticos do firmamento.",
      quoteGreeting = "O cosmos aguarda o equilíbrio das nove constelações.",
      quoteVictory = "Harmonia celestial restaurada. Você é um verdadeiro Grão-Mestre!",
      quoteCheer = "Ouça o sussurro das probabilidades.",
      unlockRequirement = "Complete o Capítulo 3 (Cidade Neon)",
      accentColorHex = 0xFFF59E0B
    )
  )

  val CHAPTERS = listOf(
    Chapter(
      id = 1,
      name = "Vila Sakura",
      subtitle = "O Despertar do Aventureiro",
      description = "Um vilarejo sereno cercado por cerejeiras em flor, onde os aspirantes aprendem a arte dos números.",
      iconEmoji = "🌸",
      themeColorHex = 0xFFF472B6,
      stages = listOf(
        Stage("c1_s1", 1, 1, "Caminho das Cerejeiras", Difficulty.EASY, xpReward = 50, coinsReward = 10),
        Stage("c1_s2", 1, 2, "Ponte do Riacho Claro", Difficulty.EASY, xpReward = 60, coinsReward = 12),
        Stage("c1_s3", 1, 3, "Mercado de Lanternas", Difficulty.MEDIUM, xpReward = 100, coinsReward = 20),
        Stage("c1_s4", 1, 4, "Santuário Antigo", Difficulty.MEDIUM, xpReward = 120, coinsReward = 25),
        Stage(
          "c1_s5", 1, 5, "Boss: Guardião Kitsune", Difficulty.MEDIUM,
          isBoss = true,
          bossName = "Kitsune Espiritual",
          bossDescription = "A raposa mágica testa sua agilidade e concentração. Máximo de 3 erros permitidos!",
          xpReward = 250, coinsReward = 60,
          specialReward = "Companheiro Ren Desbloqueado!"
        )
      )
    ),
    Chapter(
      id = 2,
      name = "Floresta Espiritual",
      subtitle = "O Enigma das Sombras",
      description = "Bosques ancestrais iluminados por vaga-lumes e raízes mágicas entrelaçadas em enigmas.",
      iconEmoji = "🍃",
      themeColorHex = 0xFF10B981,
      stages = listOf(
        Stage("c2_s1", 2, 1, "Trilha dos Vaga-lumes", Difficulty.EASY, xpReward = 70, coinsReward = 15),
        Stage("c2_s2", 2, 2, "Clareira do Carvalho", Difficulty.MEDIUM, xpReward = 110, coinsReward = 22),
        Stage("c2_s3", 2, 3, "Lago dos Reflexos", Difficulty.MEDIUM, xpReward = 130, coinsReward = 25),
        Stage("c2_s4", 2, 4, "Caverna Esmeralda", Difficulty.HARD, xpReward = 160, coinsReward = 35),
        Stage(
          "c2_s5", 2, 5, "Boss: Ent Ancestral", Difficulty.HARD,
          isBoss = true,
          bossName = "Espírito da Floresta",
          bossDescription = "O antigo guardião das árvores desafia você a dominar tabuleiros complexos com notas.",
          xpReward = 300, coinsReward = 80,
          specialReward = "Companheiro Kael Desbloqueado!"
        )
      )
    ),
    Chapter(
      id = 3,
      name = "Cidade Neon",
      subtitle = "A Matriz dos Autômatos",
      description = "Uma metrópole futurista onde circuitos cibernéticos protegem os segredos da matemática quântica.",
      iconEmoji = "⚡",
      themeColorHex = 0xFF38BDF8,
      stages = listOf(
        Stage("c3_s1", 3, 1, "Avenida dos Hologramas", Difficulty.MEDIUM, xpReward = 120, coinsReward = 25),
        Stage("c3_s2", 3, 2, "Setor Cibernético 09", Difficulty.HARD, xpReward = 170, coinsReward = 35),
        Stage("c3_s3", 3, 3, "Núcleo de Dados", Difficulty.HARD, xpReward = 180, coinsReward = 40),
        Stage("c3_s4", 3, 4, "Torre de Transmissão", Difficulty.HARD, xpReward = 200, coinsReward = 45),
        Stage(
          "c3_s5", 3, 5, "Boss: Autômato Quântico", Difficulty.EXPERT,
          isBoss = true,
          bossName = "IA Matriz Primária",
          bossDescription = "Um supercomputador que desafia suas habilidades lógicas no limite do cálculo.",
          xpReward = 350, coinsReward = 100,
          specialReward = "Companheira Aria Desbloqueada!"
        )
      )
    ),
    Chapter(
      id = 4,
      name = "Templo Celestial",
      subtitle = "O Cume dos Imortais",
      description = "Flutuando além das nuvens, o santuário onde mestres ancestrais alcançam a iluminação matemática.",
      iconEmoji = "✨",
      themeColorHex = 0xFFF59E0B,
      stages = listOf(
        Stage("c4_s1", 4, 1, "Escadaria das Estrelas", Difficulty.HARD, xpReward = 200, coinsReward = 45),
        Stage("c4_s2", 4, 2, "Portal Nebuloso", Difficulty.HARD, xpReward = 220, coinsReward = 50),
        Stage("c4_s3", 4, 3, "Altar do Sol e da Lua", Difficulty.EXPERT, xpReward = 250, coinsReward = 60),
        Stage("c4_s4", 4, 4, "Câmara do Zodíaco", Difficulty.EXPERT, xpReward = 280, coinsReward = 70),
        Stage(
          "c4_s5", 4, 5, "Boss: Dragão Celestial", Difficulty.EXPERT,
          isBoss = true,
          bossName = "Soberano Cósmico",
          bossDescription = "O enigma definitivo do Sudoku Quest. Apenas mestres lendários triunfam!",
          xpReward = 500, coinsReward = 150,
          specialReward = "Título: Mestre do Cosmos & Skin Dourada"
        )
      )
    )
  )

  val ALL_CHAPTERS: List<Chapter> get() = CHAPTERS

  val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("first_win", "Primeira Vitória", "Complete seu primeiro Sudoku na aventura.", "🌟", 1, 50, 20),
    AchievementDef("flawless_win", "Sem Erros", "Complete uma fase sem cometer nenhum erro.", "🛡️", 1, 80, 30),
    AchievementDef("three_stars", "Mente Brilhante", "Obtenha 3 estrelas em qualquer fase.", "⭐", 1, 60, 25),
    AchievementDef("fast_solver", "Velocista", "Complete uma fase em menos de 3 minutos.", "⚡", 1, 100, 40),
    AchievementDef("hard_master", "Mestre dos Números", "Complete um Sudoku de nível Difícil ou Especialista.", "🧩", 1, 150, 50),
    AchievementDef("boss_slayer", "Conquistador de Chefes", "Derrote seu primeiro Boss de capítulo.", "👑", 1, 200, 75),
    AchievementDef("chapter_1_clear", "Aventureiro da Vila", "Complete todas as 5 fases do Capítulo 1.", "🌸", 5, 250, 100),
    AchievementDef("collector", "Companhia Fiel", "Desbloqueie um novo personagem companheiro.", "👥", 2, 120, 50),
    AchievementDef("daily_streak_3", "Disciplina Diária", "Complete o Desafio Diário por 3 dias seguidos.", "🔥", 3, 150, 60),
    AchievementDef("puzzles_10", "Dedicado", "Complete 10 fases no total.", "📚", 10, 300, 100)
  )
}
