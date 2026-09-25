package com.example.aquaquestai.presentation.screens.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.presentation.components.GlassCard
import com.example.aquaquestai.theme.EmeraldHealthy
import com.example.aquaquestai.theme.QuestPurple
import com.example.aquaquestai.theme.XpGradientEnd
import com.example.aquaquestai.theme.XpGradientStart

data class QuestItem(
    val id: Int,
    val title: String,
    val description: String,
    val distance: String,
    val xpReward: Int,
    val status: QuestStatus,
    val timeLeft: String,
    val icon: String,
    val lat: Double,
    val lng: Double,
    val hazardTag: String? = null,
)

enum class QuestStatus { ACTIVE, IN_PROGRESS, COMPLETED, EXPIRED }

@Composable
fun QuestsScreen(
    viewModel: AquaQuestViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) }
    val filterOptions = listOf("All Quests", "Active", "Completed", "Expired")

    val filteredQuests = when (selectedFilterTab) {
        1 -> viewModel.quests.filter { it.status == QuestStatus.ACTIVE || it.status == QuestStatus.IN_PROGRESS }
        2 -> viewModel.quests.filter { it.status == QuestStatus.COMPLETED }
        3 -> viewModel.quests.filter { it.status == QuestStatus.EXPIRED }
        else -> viewModel.quests
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(40.dp))

        // Header
        Text(
            "🎯 Community Quests",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Verify local water anomalies & earn environmental XP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(14.dp))

        // Stats summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val activeCount = viewModel.quests.count { it.status == QuestStatus.ACTIVE || it.status == QuestStatus.IN_PROGRESS }
            val completedCount = viewModel.quests.count { it.status == QuestStatus.COMPLETED }
            val expiredCount = viewModel.quests.count { it.status == QuestStatus.EXPIRED }

            QuestStatChip("Active", activeCount.toString(), QuestPurple, Modifier.weight(1f))
            QuestStatChip("Completed", completedCount.toString(), EmeraldHealthy, Modifier.weight(1f))
            QuestStatChip("Expired", expiredCount.toString(), MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // Category Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedFilterTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            filterOptions.forEachIndexed { index, title ->
                Tab(
                    selected = selectedFilterTab == index,
                    onClick = { selectedFilterTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Quests List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(filteredQuests, key = { it.id }) { quest ->
                QuestCard(
                    quest = quest,
                    onAccept = { viewModel.acceptQuest(quest.id) },
                    onComplete = {
                        viewModel.completeQuest(quest.id)
                        onNavigateToScan()
                    },
                )
            }
        }
    }
}

@Composable
private fun QuestStatChip(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, cornerRadius = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                count,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuestCard(
    quest: QuestItem,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
) {
    val statusColor = when (quest.status) {
        QuestStatus.ACTIVE, QuestStatus.IN_PROGRESS -> QuestPurple
        QuestStatus.COMPLETED -> EmeraldHealthy
        QuestStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(quest.icon, fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    quest.hazardTag?.let { tag ->
                        Spacer(Modifier.height(3.dp))
                        androidx.compose.material3.Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        quest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.DirectionsWalk,
                        "Distance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        quest.distance,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val timeIcon = when (quest.status) {
                        QuestStatus.COMPLETED -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.Schedule
                    }
                    Icon(
                        timeIcon,
                        "Time",
                        tint = statusColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        quest.timeLeft,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(XpGradientStart, XpGradientEnd),
                            ),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "+${quest.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            // Action Buttons
            when (quest.status) {
                QuestStatus.ACTIVE -> {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(QuestPurple.copy(alpha = 0.15f))
                            .clickable(onClick = onAccept)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Accept Verification Quest",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuestPurple,
                        )
                    }
                }
                QuestStatus.IN_PROGRESS -> {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(XpGradientStart, XpGradientEnd),
                                ),
                            )
                            .clickable(onClick = onComplete)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "⚡ Complete via AI Water Scan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
