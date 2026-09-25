package com.example.aquaquestai.presentation.screens.storytelling

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.theme.ThemeState

@Composable
fun StorytellingHubScreen(
    viewModel: AquaQuestViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDark = ThemeState.isDarkMode

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0F9FF),
                Color(0xFFE0F2FE)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF0D9488)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Story Hub",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OneHealth Storytelling Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Track 4: Awareness & Storytelling",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "XP",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${viewModel.userXp} XP",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            contentColor = Color(0xFF0284C7)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📖 Stream Tales", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🎓 Micro-Academy", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🎨 Social Cards", fontWeight = FontWeight.Bold) }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> StreamTalesTab(viewModel = viewModel)
                1 -> MicroAcademyTab(viewModel = viewModel)
                2 -> SocialStoryCardsTab(
                    viewModel = viewModel,
                    onShareClick = { shareText ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share OneHealth Story Card"))
                    }
                )
            }
        }
    }
}

@Composable
private fun StreamTalesTab(viewModel: AquaQuestViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = ThemeState.isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🐾 Bio-Indicator Eco-Tales",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        val activeStory = viewModel.bioIndicatorStories[viewModel.selectedStoryIndex]

        // Character Speech Bubble Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = activeStory.narratorEmoji, fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${activeStory.narratorName} says:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    )
                    Text(
                        text = activeStory.narratorQuoteBubble,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Story Card Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF334155) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activeStory.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Narrated by ${activeStory.narratorName} (${activeStory.narratorSpecies})",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // iTunes Nature Audio Streamer Player Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0284C7),
                                    Color(0xFF0D9488)
                                )
                            )
                        )
                        .clickable { viewModel.toggleItunesStoryAudio(context, activeStory) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.isItunesAudioLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (viewModel.isAudioNarratorPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "iTunes Play",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (viewModel.isAudioNarratorPlaying) "🎵 Playing Nature Soundscape" else "▶ Stream iTunes Audio Soundscape",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                viewModel.activeItunesTrack?.let { track ->
                                    Text(
                                        text = "${track.trackName} • ${track.artistName}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f))
                                    )
                                } ?: run {
                                    Text(
                                        text = "Powered by Apple iTunes Nature Audio API",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                                    )
                                }
                            }
                        }

                        if (viewModel.isAudioNarratorPlaying) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Equalizer",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeStory.fullNarrative,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Ecological Indicator Note:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        )
                        Text(
                            text = activeStory.keyTakeaway,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF065F46))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "🩺 One Health Triad Angle:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D28D9)
                            )
                        )
                        Text(
                            text = activeStory.oneHealthAngle,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5B21B6))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Eco-Choice Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "⚡ Guardian Interactive Decision (+25 XP)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeStory.interactiveChoicePrompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        activeStory.choiceOptions.forEachIndexed { optIndex, choiceText ->
                            val isSelected = viewModel.selectedBioChoiceIndex == optIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFB45309) else Color.White)
                                    .border(1.dp, Color(0xFFB45309), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.submitBioIndicatorChoice(activeStory.id, optIndex)
                                    }
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = choiceText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }

                        viewModel.bioChoiceFeedbackMessage?.let { feedback ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Story Switcher Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            viewModel.bioIndicatorStories.forEachIndexed { index, story ->
                Button(
                    onClick = {
                        viewModel.selectedStoryIndex = index
                        viewModel.selectedBioChoiceIndex = -1
                        viewModel.bioChoiceFeedbackMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.selectedStoryIndex == index) Color(0xFF0284C7) else Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("${story.narratorEmoji} ${story.narratorName}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // European Pilot Cities Section
        Text(
            text = "🇪🇺 European Pilot Cities Transformation Stories",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        val activeCity = viewModel.euPilotCityTales[viewModel.selectedEuCityIndex]

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = activeCity.countryFlag, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${activeCity.cityName} (${activeCity.riverBasin})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = activeCity.historicalTimelineYear,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D9488))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Status: ${activeCity.ecoStatus}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeCity.transformationStory,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🇪🇺 ${activeCity.wfdMeasureCode}: ${activeCity.wfdMeasureDescription}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "🎯 Restoration Target: ${activeCity.restorationGoal}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF047857)
                    )
                )
            }
        }

        // City Switcher Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            viewModel.euPilotCityTales.forEachIndexed { index, city ->
                Text(
                    text = "${city.countryFlag} ${city.cityName}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (viewModel.selectedEuCityIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (viewModel.selectedEuCityIndex == index) Color(0xFF0284C7) else Color.Gray
                    ),
                    modifier = Modifier
                        .clickable { viewModel.selectedEuCityIndex = index }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun MicroAcademyTab(viewModel: AquaQuestViewModel) {
    val scrollState = rememberScrollState()
    val isDark = ThemeState.isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎓 OneHealth Micro-Academy",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        val activeModule = viewModel.academyModules[viewModel.selectedAcademyModuleIndex]
        val isCompleted = viewModel.completedQuizIds.contains(activeModule.id)

        // Module Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF334155) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = activeModule.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeModule.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeModule.fullBodyText,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Quiz Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "❓ Micro-Quiz Challenge (+50 XP)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeModule.quiz.questionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        activeModule.quiz.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = viewModel.selectedQuizOptionIndex == optIndex
                            val btnBg = if (isSelected) Color(0xFF0284C7) else Color.White

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(btnBg)
                                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.submitAcademyQuizAnswer(activeModule.id, optIndex)
                                    }
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "${('A' + optIndex)}. $optionText",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }

                        // Feedback Banner
                        viewModel.quizFeedbackMessage?.let { feedback ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (viewModel.isQuizCorrect == true) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = feedback,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Module Selector Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            viewModel.academyModules.forEachIndexed { index, module ->
                Button(
                    onClick = {
                        viewModel.selectedAcademyModuleIndex = index
                        viewModel.quizFeedbackMessage = null
                        viewModel.selectedQuizOptionIndex = -1
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.selectedAcademyModuleIndex == index) Color(0xFF0284C7) else Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Module ${index + 1}")
                }
            }
        }
    }
}

@Composable
private fun SocialStoryCardsTab(
    viewModel: AquaQuestViewModel,
    onShareClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = ThemeState.isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎨 1-Tap Social Story Card Builder",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Button(
            onClick = { viewModel.generateStoryCardForCurrentLocation("Colorado River Sector 4") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Generate")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Fresh Story Card", fontWeight = FontWeight.Bold)
        }

        viewModel.generatedSocialCard?.let { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = card.statusBadge,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        )
                        Text(
                            text = card.petSafetyStatus,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = card.headline,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "📍 ${card.streamName} • ${card.ecoMetricHighlight}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = card.formattedShareText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onShareClick(card.formattedShareText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share to Social Media (Instagram / X / LinkedIn)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap 'Generate Fresh Story Card' above to create a shareable social infographic!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}
