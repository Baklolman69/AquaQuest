package com.example.aquaquestai.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.theme.EmeraldHealthy
import com.example.aquaquestai.theme.XpGradientEnd
import com.example.aquaquestai.theme.XpGradientStart

/**
 * Full-screen level-up celebration overlay with confetti particles
 * and a scale-in glassmorphism card showing the new level and tier badge.
 */
@Composable
fun LevelUpCelebrationOverlay(
    isVisible: Boolean,
    newLevel: Int,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    var triggerAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        triggerAnimation = isVisible
    }

    val scale by animateFloatAsState(
        targetValue = if (triggerAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "levelup_scale",
    )

    val tierName = when {
        newLevel >= 20 -> "🏆 Diamond Guardian"
        newLevel >= 15 -> "🥇 Gold Defender"
        newLevel >= 10 -> "🥈 Silver Ranger"
        newLevel >= 5 -> "🥉 Bronze Scout"
        else -> "🌱 Stream Apprentice"
    }

    val tierColor = when {
        newLevel >= 20 -> Color(0xFFB9F2FF) // Diamond
        newLevel >= 15 -> Color(0xFFFFD700) // Gold
        newLevel >= 10 -> Color(0xFFC0C0C0) // Silver
        newLevel >= 5 -> Color(0xFFCD7F32)  // Bronze
        else -> EmeraldHealthy
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        // Confetti behind the card
        ConfettiOverlay(
            isVisible = true,
            durationMs = 4000,
        )

        // Celebration Card
        Column(
            modifier = Modifier
                .scale(scale)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ),
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "🎉",
                fontSize = 56.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "LEVEL UP!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            // Level Number with gradient
            Text(
                text = "Level $newLevel",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = tierColor,
            )

            Spacer(Modifier.height(8.dp))

            // Tier Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(XpGradientStart, XpGradientEnd),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = tierName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Your contributions to water health monitoring are making a real difference!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            // Continue Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onDismiss() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Continue 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
