package com.example.aquaquestai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aquaquestai.theme.ThemeState

/**
 * Modern card component optimized for both Light Mode and Dark Mode.
 * In Light mode: Soft elevated white container with crisp border.
 * In Dark mode: Frosted glass container with cyan glow border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = ThemeState.isDarkMode
    val shape = RoundedCornerShape(cornerRadius)

    val containerBg = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val outlineColor = borderColor ?: if (isDark) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }

    val shadowElevation = if (isDark) 0.dp else 4.dp

    Box(
        modifier = modifier
            .shadow(shadowElevation, shape, clip = false)
            .clip(shape)
            .background(containerBg)
            .border(
                width = 1.dp,
                color = outlineColor,
                shape = shape,
            )
            .padding(16.dp),
        content = content,
    )
}
