package com.example.aquaquestai.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── AquaQuest Light Mode Theme ──────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryDim,
    onPrimaryContainer = LightTextPrimary,
    secondary = QuestPurple,
    onSecondary = Color.White,
    secondaryContainer = QuestPurple.copy(alpha = 0.15f),
    onSecondaryContainer = QuestPurple,
    tertiary = EmeraldHealthy,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldHealthy.copy(alpha = 0.15f),
    onTertiaryContainer = EmeraldHealthy,
    error = AlertCrimson,
    onError = Color.White,
    errorContainer = AlertCrimson.copy(alpha = 0.15f),
    onErrorContainer = AlertCrimson,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightTextMuted,
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightBackground,
    inversePrimary = LightPrimaryVariant,
    surfaceTint = LightPrimary,
    scrim = Color(0x33000000),
)

// ── AquaQuest Dark Mode Theme ───────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = BioCyanPrimary,
    onPrimary = OceanDarkBackground,
    primaryContainer = BioCyanDim,
    onPrimaryContainer = TextPrimaryWhite,
    secondary = QuestPurple,
    onSecondary = TextPrimaryWhite,
    secondaryContainer = QuestPurple.copy(alpha = 0.25f),
    onSecondaryContainer = QuestPurpleLight,
    tertiary = EmeraldHealthy,
    onTertiary = OceanDarkBackground,
    tertiaryContainer = EmeraldHealthy.copy(alpha = 0.2f),
    onTertiaryContainer = EmeraldLight,
    error = AlertCrimson,
    onError = TextPrimaryWhite,
    errorContainer = AlertCrimson.copy(alpha = 0.2f),
    onErrorContainer = AlertCrimsonLight,
    background = OceanDarkBackground,
    onBackground = TextPrimaryWhite,
    surface = OceanCardSurface,
    onSurface = TextPrimaryWhite,
    surfaceVariant = OceanDeepCard,
    onSurfaceVariant = TextSecondaryMuted,
    outline = GlassBorderOverlay,
    outlineVariant = TextTertiary,
    inverseSurface = TextPrimaryWhite,
    inverseOnSurface = OceanDarkBackground,
    inversePrimary = BioCyanDim,
    surfaceTint = BioCyanPrimary,
    scrim = Color(0xCC000000),
)

@Composable
fun AquaQuestAITheme(
    darkTheme: Boolean = ThemeState.isDarkMode,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AquaQuestTypography,
        content = content,
    )
}
