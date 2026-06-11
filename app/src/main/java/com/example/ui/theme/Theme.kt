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

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberIndigo,
    secondary = CyberCyan,
    tertiary = NeonGreen,
    background = SpaceDarkBackground,
    surface = CardBackground,
    onPrimary = PureWhite,
    onSecondary = SpaceDarkBackground,
    onBackground = LightGreyText,
    onSurface = PureWhite
  )

private val LightColorScheme = DarkColorScheme // Forced dark theme for premium cybernetic experience

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for professional cyber dashboard
  dynamicColor: Boolean = false, // Force custom palette instead of dynamic wallpaper tints
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
