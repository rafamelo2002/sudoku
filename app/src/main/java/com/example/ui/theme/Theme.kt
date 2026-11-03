package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = IndigoPrimaryDark,
  onPrimary = IndigoOnPrimaryDark,
  primaryContainer = IndigoPrimaryContainerDark,
  onPrimaryContainer = IndigoOnPrimaryContainerDark,
  secondary = GoldSecondaryDark,
  onSecondary = GoldOnSecondaryDark,
  secondaryContainer = GoldSecondaryContainerDark,
  onSecondaryContainer = GoldOnSecondaryContainerDark,
  tertiary = SakuraPink,
  onTertiary = QuestBackgroundDark,
  background = QuestBackgroundDark,
  surface = QuestSurfaceDark
)

private val LightColorScheme = lightColorScheme(
  primary = IndigoPrimaryLight,
  onPrimary = IndigoOnPrimaryLight,
  primaryContainer = IndigoPrimaryContainerLight,
  onPrimaryContainer = IndigoOnPrimaryContainerLight,
  secondary = GoldSecondaryLight,
  onSecondary = GoldOnSecondaryLight,
  secondaryContainer = GoldSecondaryContainerLight,
  onSecondaryContainer = GoldOnSecondaryContainerLight,
  tertiary = SakuraPink,
  onTertiary = QuestBackgroundLight,
  background = QuestBackgroundLight,
  surface = QuestSurfaceLight
)

@Composable
fun SudokuTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
