package com.example.aquaquestai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.domain.model.WaterStoryStep
import com.example.aquaquestai.theme.EmeraldHealthy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterStoryTimelineComponent(
    steps: List<WaterStoryStep>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantBg = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceVariantBg)
            .border(1.dp, outlineBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "📜 Water Story Event Timeline",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor
        )
        Text(
            text = "Chronological evidence trail from baseline monitoring to verification",
            fontSize = 12.sp,
            color = onSurfaceMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        steps.forEachIndexed { index, step ->
            val isLast = index == steps.size - 1
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Timeline indicator column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (step.category == "STATUS") EmeraldHealthy.copy(alpha = 0.18f)
                                else primaryColor.copy(alpha = 0.18f)
                            )
                            .border(
                                1.dp,
                                if (step.category == "STATUS") EmeraldHealthy else primaryColor,
                                CircleShape
                            )
                    ) {
                        Text(text = step.iconName, fontSize = 12.sp)
                    }

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(44.dp)
                                .background(primaryColor.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Timeline step content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (!isLast) 12.dp else 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = step.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (step.timestamp > 0) dateFormat.format(Date(step.timestamp)) else step.timeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    if (step.description.isNotEmpty()) {
                        Text(
                            text = step.description,
                            fontSize = 12.sp,
                            color = onSurfaceMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
