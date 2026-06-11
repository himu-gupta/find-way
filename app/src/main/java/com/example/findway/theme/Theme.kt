package com.example.findway.theme

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
    primary = Forest80,
    onPrimary = OnForestContainerLight,
    primaryContainer = Forest40,
    onPrimaryContainer = Forest80,
    secondary = Sky80,
    secondaryContainer = Sky40,
    onSecondaryContainer = Sky80,
    tertiary = SafetyOrange80,
    tertiaryContainer = SafetyOrange40,
    onTertiaryContainer = OrangeContainerLight,
    background = TrailSurfaceDark,
    surface = TrailSurfaceDark,
    surfaceContainer = TrailSurfaceContainerDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Forest40,
    onPrimary = TrailSurfaceLight,
    primaryContainer = ForestContainerLight,
    onPrimaryContainer = OnForestContainerLight,
    secondary = Sky40,
    onSecondary = TrailSurfaceLight,
    secondaryContainer = SkyContainerLight,
    onSecondaryContainer = OnSkyContainerLight,
    tertiary = SafetyOrange40,
    onTertiary = TrailSurfaceLight,
    tertiaryContainer = OrangeContainerLight,
    onTertiaryContainer = OnOrangeContainerLight,
    background = TrailSurfaceLight,
    surface = TrailSurfaceLight,
    surfaceContainer = TrailSurfaceContainerLight,
    outline = TrailOutlineLight,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
  )

@Composable
fun FindWayTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
