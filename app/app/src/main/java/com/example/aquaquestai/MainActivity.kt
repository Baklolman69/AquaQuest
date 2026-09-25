package com.example.aquaquestai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquaquestai.data.firebase.FirebaseRepository
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.presentation.components.AquaQuestBottomBar
import com.example.aquaquestai.presentation.components.LevelUpCelebrationOverlay
import com.example.aquaquestai.presentation.screens.home.HomeScreen
import com.example.aquaquestai.presentation.screens.splash.SplashScreen
import com.example.aquaquestai.presentation.screens.onboarding.OnboardingScreen
import com.example.aquaquestai.presentation.screens.aquahealth.AquaHealthScreen
import com.example.aquaquestai.presentation.screens.camera.CameraScreen
import com.example.aquaquestai.presentation.screens.community.AquaCommunityScreen
import com.example.aquaquestai.presentation.screens.map.MapScreen
import com.example.aquaquestai.presentation.screens.profile.ProfileScreen
import com.example.aquaquestai.presentation.screens.quests.QuestsScreen
import com.example.aquaquestai.presentation.screens.research.ResearchNewsScreen
import com.example.aquaquestai.presentation.screens.storytelling.StorytellingHubScreen
import com.example.aquaquestai.theme.AquaQuestAITheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure osmdroid User-Agent policy according to OSM usage guidelines
        Configuration.getInstance().apply {
            userAgentValue = "AquaQuestAI-Mobile-CitizenScience/1.0 (https://aquaquestai.org; contact@aquaquestai.org)"
            osmdroidTileCache = cacheDir
        }

        enableEdgeToEdge()
        setContent {
            AquaQuestAITheme {
                AquaQuestApp()
            }
        }
    }
}

@Composable
fun AquaQuestApp() {
    val context = LocalContext.current
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewModel: AquaQuestViewModel = viewModel()

    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
    } else if (showOnboarding) {
        OnboardingScreen(
            onOnboardingCompleted = { _ ->
                showOnboarding = false
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    AquaQuestBottomBar(
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToMap = { selectedTab = 1 },
                            onNavigateToScan = { selectedTab = 2 },
                            onNavigateToCommunity = { selectedTab = 5 },
                            onNavigateToQuests = { selectedTab = 3 },
                            onNavigateToStories = { selectedTab = 6 },
                            onNavigateToProfile = { selectedTab = 4 },
                            onNavigateToAquaHealth = { selectedTab = 7 },
                            onNavigateToResearchNews = { selectedTab = 8 }
                        )
                        1 -> MapScreen(
                            viewModel = viewModel,
                            onNavigateToScan = { selectedTab = 2 },
                            onNavigateToAquaHealth = { selectedTab = 7 }
                        )
                        2 -> CameraScreen(
                            viewModel = viewModel,
                            onNavigateToAquaHealth = { selectedTab = 7 }
                        )
                        3 -> QuestsScreen(
                            viewModel = viewModel,
                            onNavigateToScan = { selectedTab = 2 },
                        )
                        4 -> ProfileScreen(
                            viewModel = viewModel,
                        )
                        5 -> AquaCommunityScreen(
                            viewModel = viewModel,
                            onNavigateToScan = { selectedTab = 2 }
                        )
                        6 -> StorytellingHubScreen(
                            viewModel = viewModel,
                        )
                        7 -> AquaHealthScreen(
                            viewModel = viewModel,
                        )
                        8 -> ResearchNewsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { selectedTab = 0 }
                        )
                    }
                }
            }

            // ── Level-Up Celebration Overlay (renders above everything) ──
            LevelUpCelebrationOverlay(
                isVisible = viewModel.showLevelUpCelebration,
                newLevel = viewModel.userLevel,
                onDismiss = { viewModel.dismissLevelUpCelebration() },
            )
        }
    }
}
