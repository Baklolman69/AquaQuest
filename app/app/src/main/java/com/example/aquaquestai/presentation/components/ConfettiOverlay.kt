package com.example.aquaquestai.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,           // 0..1 normalized starting X
    val y: Float,           // starting Y (negative = above screen)
    val velocityX: Float,   // horizontal drift per frame
    val velocityY: Float,   // fall speed per frame
    val rotation: Float,    // initial rotation
    val rotationSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val isCircle: Boolean,
)

/**
 * Full-screen confetti particle overlay using Compose Canvas.
 * Renders 60 randomized confetti pieces with gravity, rotation, and color variation.
 * Auto-animates for [durationMs] then calls [onFinished].
 */
@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    durationMs: Int = 3500,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return

    val confettiColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFF00E5A0), // Emerald
        Color(0xFF00BCD4), // Cyan
        Color(0xFFA855F7), // Purple
        Color(0xFFFF6B6B), // Coral
        Color(0xFF3B82F6), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
    )

    val particles = remember {
        List(65) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f - 0.1f, // Start above screen
                velocityX = (Random.nextFloat() - 0.5f) * 0.012f,
                velocityY = Random.nextFloat() * 0.008f + 0.005f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                width = Random.nextFloat() * 12f + 6f,
                height = Random.nextFloat() * 8f + 4f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                isCircle = Random.nextFloat() > 0.65f,
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMs, easing = LinearEasing),
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 0.0015f

        particles.forEach { p ->
            val elapsed = t * 120f // ~120 virtual frames over duration
            val px = (p.x + p.velocityX * elapsed) * size.width
            val py = (p.y + p.velocityY * elapsed + 0.5f * gravity * elapsed * elapsed) * size.height
            val rot = p.rotation + p.rotationSpeed * elapsed

            // Only draw if on-screen
            if (py > -50f && py < size.height + 50f) {
                rotate(rot, pivot = Offset(px, py)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color.copy(alpha = (1f - t * 0.6f).coerceIn(0.1f, 1f)),
                            radius = p.width / 2f,
                            center = Offset(px, py),
                        )
                    } else {
                        drawRect(
                            color = p.color.copy(alpha = (1f - t * 0.6f).coerceIn(0.1f, 1f)),
                            topLeft = Offset(px - p.width / 2f, py - p.height / 2f),
                            size = Size(p.width, p.height),
                        )
                    }
                }
            }
        }
    }
}
