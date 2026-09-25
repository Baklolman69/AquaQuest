package com.example.aquaquestai.presentation.screens.map

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.aquaquestai.data.OverpassWaterQueryEngine
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.presentation.MapFilter
import com.example.aquaquestai.presentation.components.DataExportDialog
import com.example.aquaquestai.presentation.components.GlassCard
import com.example.aquaquestai.presentation.components.GroqDoctorSheet
import com.example.aquaquestai.presentation.components.XpProgressBar
import com.example.aquaquestai.theme.AlertCrimson
import com.example.aquaquestai.theme.AquaPrimary
import com.example.aquaquestai.theme.EmeraldHealthy

import com.example.aquaquestai.theme.QuestPurple
import com.example.aquaquestai.theme.ThemeState
import com.example.aquaquestai.theme.WarningAmber
import com.example.aquaquestai.theme.XpGradientEnd
import com.example.aquaquestai.theme.XpGradientStart
import com.example.aquaquestai.theme.LightPrimary
import com.example.aquaquestai.theme.LightBorder
import com.example.aquaquestai.theme.LightTextMuted
import com.example.aquaquestai.theme.LightTextPrimary
import com.example.aquaquestai.theme.LightTextSecondary
import kotlinx.coroutines.launch

private fun createMarkerDrawable(type: MarkerType): android.graphics.drawable.Drawable {
    val color = when (type) {
        MarkerType.HEALTHY -> AndroidColor.parseColor("#10B981")
        MarkerType.WARNING -> AndroidColor.parseColor("#F59E0B")
        MarkerType.ALERT -> AndroidColor.parseColor("#EF4444")
        MarkerType.QUEST -> AndroidColor.parseColor("#8B5CF6")
    }

    val strokeColor = when (type) {
        MarkerType.HEALTHY -> AndroidColor.parseColor("#34D399")
        MarkerType.WARNING -> AndroidColor.parseColor("#FBBF24")
        MarkerType.ALERT -> AndroidColor.parseColor("#F87171")
        MarkerType.QUEST -> AndroidColor.parseColor("#A78BFA")
    }

    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(4, strokeColor)
        setSize(48, 48)
    }
}


data class MapMarkerData(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val title: String,
    val snippet: String,
    val type: MarkerType,
    val ph: Double = 7.2,
    val turbidity: String = "1.5 NTU",
    val microplasticRisk: String = "Low",
    val author: String = "AquaScout",
    val timestamp: String = "1 hour ago",
)

enum class MarkerType { HEALTHY, WARNING, ALERT, QUEST }

enum class FeedCardState {
    INITIAL_DELAY,
    CARD_1_VISIBLE,
    CARD_1_COMPLETING,
    INTER_DELAY,
    CARD_2_VISIBLE,
    CARD_2_COMPLETING,
    COMPLETED_ALL
}

@Composable
fun MapScreen(
    viewModel: AquaQuestViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToAquaHealth: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = ThemeState.isDarkMode
    val selectedMarker = viewModel.selectedMarker
    var showExportDialog by remember { mutableStateOf(false) }
    var showGroqDoctorSheet by remember { mutableStateOf(false) }
    var activeUsgsDetailReport by remember { mutableStateOf<com.example.aquaquestai.data.UsgsWaterHealthReport?>(null) }
    var feedState by remember { mutableStateOf(FeedCardState.INITIAL_DELAY) }

    androidx.compose.runtime.LaunchedEffect(feedState) {
        when (feedState) {
            FeedCardState.INITIAL_DELAY -> {
                kotlinx.coroutines.delay(3500)
                feedState = FeedCardState.CARD_1_VISIBLE
            }
            FeedCardState.CARD_1_COMPLETING -> {
                com.example.aquaquestai.data.firebase.FirebaseRepository.recordFeedQuestCompletion(context, "Verify Turbidity Spike at River Park", 100)
                kotlinx.coroutines.delay(1800)
                feedState = FeedCardState.INTER_DELAY
            }
            FeedCardState.INTER_DELAY -> {
                kotlinx.coroutines.delay(2500)
                feedState = FeedCardState.CARD_2_VISIBLE
            }
            FeedCardState.CARD_2_COMPLETING -> {
                com.example.aquaquestai.data.firebase.FirebaseRepository.recordFeedQuestCompletion(context, "Log Stream Bio-Indicator Photo", 75)
                kotlinx.coroutines.delay(1800)
                feedState = FeedCardState.COMPLETED_ALL
            }
            else -> {}
        }
    }


    if (activeUsgsDetailReport != null) {
        UsgsWaterDetailScreen(
            report = activeUsgsDetailReport!!,
            onBack = { activeUsgsDetailReport = null },
            modifier = modifier
        )
        return
    }

    if (showExportDialog) {
        DataExportDialog(
            markers = viewModel.mapMarkers,
            onDismiss = { showExportDialog = false },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── Real OpenStreetMap (osmdroid) Engine ──
        val currentCity = viewModel.selectedCity
        val currentMarkers = viewModel.filteredMarkers
        val currentHeatmap = viewModel.isHeatmapMode

        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = "AquaQuestAI-Mobile-CitizenScience/1.0 (contact@aquaquestai.org)"
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(currentCity.defaultZoom.toDouble())
                    controller.setCenter(GeoPoint(currentCity.latitude, currentCity.longitude))
                    minZoomLevel = 4.0
                    maxZoomLevel = 19.0
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.setTileSource(TileSourceFactory.MAPNIK)

                if (isDark) {
                    mapView.overlayManager.tilesOverlay.setColorFilter(
                        android.graphics.ColorMatrixColorFilter(
                            floatArrayOf(
                                -0.8f, 0f, 0f, 0f, 255f,
                                0f, -0.8f, 0f, 0f, 255f,
                                0f, 0f, -0.7f, 0f, 255f,
                                0f, 0f, 0f, 1f, 0f,
                            )
                        )
                    )
                } else {
                    mapView.overlayManager.tilesOverlay.setColorFilter(null)
                }

                mapView.overlays.clear()

                // Add city-specific river channel polyline
                mapView.overlays.add(OverpassWaterQueryEngine.getRiverChannelPolyline(currentCity.id))

                // Add observation markers
                currentMarkers.forEach { data ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(data.lat, data.lng)
                        title = data.title
                        snippet = data.snippet
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = createMarkerDrawable(data.type)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.selectedMarker = data
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // Center map on selected EU Pilot City with proper zoom
                val cityCenter = GeoPoint(currentCity.latitude, currentCity.longitude)
                mapView.controller.setZoom(currentCity.defaultZoom.toDouble())
                mapView.controller.animateTo(cityCenter)

                mapView.invalidate()
            },
        )

        // ══════════════════════════════════════════════════════════════
        // ██  TOP OVERLAY: App Bar + Location + Search + Chips       ██
        // ══════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            // ── 1. Premium Top App Bar ──
            PremiumTopAppBar(
                viewModel = viewModel,
                onMenuClick = { ThemeState.toggleTheme() },
                onExportClick = { showExportDialog = true },
                onAquaHealthClick = onNavigateToAquaHealth,
            )

            // ── 2. Location & Weather Info Row ──
            LocationWeatherRow(
                cityName = "${viewModel.selectedCity.riverSystem}, ${viewModel.selectedCity.name}",
                countryFlag = viewModel.selectedCity.countryFlag,
            )

            // ── 3. Search Bar ──
            MapSearchBar(
                onReportFetched = { report ->
                    viewModel.addObservation(
                        title = report.locationName,
                        snippet = "USGS Station ${report.siteId} • Flow: ${report.streamFlowCfs?.let { "$it cfs" } ?: "N/A"}",
                        type = if (report.oneHealthRiskScore <= 3) MarkerType.HEALTHY else MarkerType.WARNING,
                        ph = report.ph ?: 7.0,
                        turbidity = report.turbidityNtu?.let { "$it NTU" } ?: "N/A",
                        microplasticRisk = report.dissolvedOxygenMgL?.let { "Diss. O2: $it mg/L" } ?: "Diss. O2: N/A"
                    )
                },
                onCardClick = { report ->
                    activeUsgsDetailReport = report
                }
            )

            // ── 5. Map Filter Chips ──
            MapFilterChipsRow(
                selectedFilter = viewModel.selectedMapFilter,
                onFilterSelected = { viewModel.selectedMapFilter = it },
            )
        }

        // ══════════════════════════════════════════════════════════════
        // ██  RIGHT SIDE: Map Control Buttons (Compass, Zoom, AI)    ██
        // ══════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .offset(y = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Compass / Navigation
            MapControlButton(
                onClick = { /* center on location */ },
                backgroundColor = Color.White,
                contentColor = LightPrimary,
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = "Navigate", modifier = Modifier.size(20.dp))
            }

            // Zoom In
            MapControlButton(
                onClick = { /* zoom in */ },
                backgroundColor = Color.White,
                contentColor = Color(0xFF475569),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            // Zoom Out
            MapControlButton(
                onClick = { /* zoom out */ },
                backgroundColor = Color.White,
                contentColor = Color(0xFF475569),
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }
        }

        // ── AI Floating Action Button (Bottom Right) ──
        FloatingActionButton(
            onClick = { showGroqDoctorSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 230.dp)
                .size(52.dp),
            containerColor = Color(0xFF4F6BED),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("✦", fontSize = 16.sp, color = Color.White)
                Text("AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }


        // ══════════════════════════════════════════════════════════════
        // ██  BOTTOM OVERLAY: Live Data + Alert + Quest Cards        ██
        // ══════════════════════════════════════════════════════════════

        // ── Active AquaEvent Sheet Overlay ──
        AnimatedVisibility(
            visible = viewModel.selectedAquaEvent != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            viewModel.selectedAquaEvent?.let { event ->
                AquaEventDetailSheet(
                    event = event,
                    onDismiss = { viewModel.selectedAquaEvent = null },
                    onVerifyClick = { eventId, voteType ->
                        viewModel.submitVerificationVote(eventId, voteType)
                        viewModel.selectedAquaEvent = null
                    }
                )
            }
        }

        // ── Groq AI Doctor Sheet Overlay ──
        AnimatedVisibility(
            visible = showGroqDoctorSheet,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            GroqDoctorSheet(
                viewModel = viewModel,
                onClose = { showGroqDoctorSheet = false },
            )
        }

        // ── Selected Marker Detail Card ──
        if (!showGroqDoctorSheet && viewModel.selectedAquaEvent == null) {
            AnimatedVisibility(
                visible = selectedMarker != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                selectedMarker?.let { marker ->
                    MarkerDetailCard(
                        marker = marker,
                        onClose = { viewModel.selectedMarker = null },
                        onVerify = {
                            viewModel.selectedMarker = null
                            onNavigateToScan()
                        },
                    )
                }
            }

            // ── Bottom Cards Stack: Live Data + Alert + Quest ──
            if (selectedMarker == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // ── Live River Data Pill ──
                    LiveRiverDataPill(
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // ── AquaEvent Alert Card (Sediment Runoff Anomaly style) ──
                    if (viewModel.activeAquaEvents.isNotEmpty()) {
                        val topEvent = viewModel.activeAquaEvents.first()
                        SedimentAlertCard(
                            event = topEvent,
                            onClick = { viewModel.selectedAquaEvent = topEvent },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    // ── Active Quest Card (Blue gradient) ──
                    if (feedState == FeedCardState.CARD_1_VISIBLE || feedState == FeedCardState.CARD_1_COMPLETING || feedState == FeedCardState.CARD_2_VISIBLE || feedState == FeedCardState.CARD_2_COMPLETING) {
                        ActiveQuestCard(
                            feedState = feedState,
                            onCompleteQuest = {
                                if (feedState == FeedCardState.CARD_1_VISIBLE) {
                                    feedState = FeedCardState.CARD_1_COMPLETING
                                } else if (feedState == FeedCardState.CARD_2_VISIBLE) {
                                    feedState = FeedCardState.CARD_2_COMPLETING
                                }
                                onNavigateToScan()
                            }
                        )
                    }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Premium Top App Bar               ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumTopAppBar(
    viewModel: AquaQuestViewModel,
    onMenuClick: () -> Unit,
    onExportClick: () -> Unit,
    onAquaHealthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Menu / Theme toggle
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (ThemeState.isDarkMode) Icons.Filled.LightMode else Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Water drop icon + Title
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💧", fontSize = 16.sp)
                }

                Spacer(Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AquaQuest",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                        )
                    }
                    Text(
                        text = "Cleaner Water • Healthier Communities • A Safer Tomorrow",
                        fontSize = 8.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Level Badge
                Surface(
                    modifier = Modifier.padding(end = 4.dp),
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🏆", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Lv ${viewModel.userLevel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                            )
                            XpProgressBar(
                                progress = viewModel.userXp.toFloat() / viewModel.maxLevelXp,
                                modifier = Modifier.width(60.dp),
                                height = 3.dp,
                            )
                            Text(
                                text = "${viewModel.userXp} / ${viewModel.maxLevelXp} XP",
                                fontSize = 7.sp,
                                color = Color(0xFF94A3B8),
                            )
                        }
                    }
                }

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                            )
                        )
                        .clickable { onAquaHealthClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🌊", fontSize = 16.sp)
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Location & Weather Row            ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun LocationWeatherRow(
    cityName: String,
    countryFlag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Current Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F9FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📍", fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Current Location",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$countryFlag $cityName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(140.dp),
                        )
                        Text(" ▾", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            // Weather Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌤️", fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "72°F",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                    )
                    Text(
                        text = "Partly Cloudy",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8),
                    )
                }
            }

            // Air Quality Badge
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Good",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                    )
                    Text(
                        text = "Air Quality",
                        fontSize = 8.sp,
                        color = Color(0xFF6EE7B7),
                    )
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Map Search Bar                    ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun MapSearchBar(
    onReportFetched: (com.example.aquaquestai.data.UsgsWaterHealthReport) -> Unit,
    onCardClick: (com.example.aquaquestai.data.UsgsWaterHealthReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search city or station (e.g. Austin, Denver, Hudson)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        keyboardController?.hide()
                        isLoading = true
                        scope.launch {
                            val report = com.example.aquaquestai.data.UsgsWaterService.fetchWaterDataByLocation(searchQuery)
                            isLoading = false
                            onReportFetched(report)
                        }
                    }
                }),
                modifier = Modifier.weight(1f),
            )

            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF0284C7),
                    strokeWidth = 2.dp,
                )
            } else {
                // Filter icon button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F9FF))
                        .clickable { /* open filter options */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Filter Chips Row                  ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun MapFilterChipsRow(
    selectedFilter: MapFilter,
    onFilterSelected: (MapFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PillChip(
            label = "All Pins",
            dotColor = null,
            isSelected = selectedFilter == MapFilter.ALL,
            onClick = { onFilterSelected(MapFilter.ALL) },
        )
        PillChip(
            label = "Healthy",
            dotColor = EmeraldHealthy,
            isSelected = selectedFilter == MapFilter.HEALTHY,
            onClick = { onFilterSelected(MapFilter.HEALTHY) },
        )
        PillChip(
            label = "Warning",
            dotColor = WarningAmber,
            isSelected = selectedFilter == MapFilter.WARNING,
            onClick = { onFilterSelected(MapFilter.WARNING) },
        )
        PillChip(
            label = "Alerts",
            dotColor = AlertCrimson,
            isSelected = selectedFilter == MapFilter.ALERT,
            onClick = { onFilterSelected(MapFilter.ALERT) },
        )
        PillChip(
            label = "Quests",
            dotColor = QuestPurple,
            isSelected = selectedFilter == MapFilter.QUEST,
            onClick = { onFilterSelected(MapFilter.QUEST) },
        )
    }
}

@Composable
private fun PillChip(
    label: String,
    dotColor: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) Color(0xFF0284C7) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF475569)
    val borderCol = if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else dotColor)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Map Control Button                ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun MapControlButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor,
            ) {
                content()
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Live River Data Pill              ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun LiveRiverDataPill(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "live_dot_pulse",
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { },
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Water wave icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F9FF)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🌊", fontSize = 14.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live River Data",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing green dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .alpha(pulseAlpha)
                            .background(EmeraldHealthy),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Monitoring 24/7",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██               COMPONENT: Sediment Alert Card                    ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun SedimentAlertCard(
    event: com.example.aquaquestai.domain.model.AquaEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Alert Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AlertCrimson.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🚨", fontSize = 16.sp)
            }

            Spacer(Modifier.width(10.dp))

            // Title & Location
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 10.sp)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${event.locationName} • ${event.status.displayName}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Thumbnail placeholder (river image)
            Box(
                modifier = Modifier
                    .size(48.dp, 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF93C5FD), Color(0xFF38BDF8), Color(0xFF0EA5E9))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("🏞️", fontSize = 18.sp)
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Active Quest Card                  ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ActiveQuestCard(
    feedState: FeedCardState,
    onCompleteQuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleting = feedState == FeedCardState.CARD_1_COMPLETING || feedState == FeedCardState.CARD_2_COMPLETING
    val title = if (feedState == FeedCardState.CARD_2_VISIBLE || feedState == FeedCardState.CARD_2_COMPLETING) {
        "Log Stream Bio-Indicator Photo"
    } else {
        "Verify Turbidity Spike at River Park"
    }

    val distance = if (feedState == FeedCardState.CARD_2_VISIBLE || feedState == FeedCardState.CARD_2_COMPLETING) "0.8 km away" else "1.4 km away"
    val xpText = if (feedState == FeedCardState.CARD_2_VISIBLE || feedState == FeedCardState.CARD_2_COMPLETING) "+75 XP" else "+100 XP"
    val btnText = if (feedState == FeedCardState.CARD_2_VISIBLE || feedState == FeedCardState.CARD_2_COMPLETING) "Capture Stream Photo" else "Start Verification Quest"

    val infiniteTransition = rememberInfiniteTransition(label = "quest_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "quest_card_pulse",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (isCompleting) 1.02f else pulseScale),
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isCompleting) listOf(Color(0xFF10B981), Color(0xFF34D399))
                        else listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                if (isCompleting) {
                    // ── Completion State ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Text("🎉", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Task Complete! $xpText",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                "Recorded & synced to Firebase Firestore",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                } else {
                    // ── Active Quest Layout ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Active Quest Nearby!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 10.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = distance,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                                Spacer(Modifier.weight(1f))
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = xpText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(56.dp, 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF93C5FD).copy(alpha = 0.5f), Color(0xFF3B82F6).copy(alpha = 0.3f))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🏞️", fontSize = 22.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // CTA Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onCompleteQuest),
                        color = Color(0xFF7C3AED),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("▶", fontSize = 12.sp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = btnText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("›", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    COMPONENT: Marker Detail Card                ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun MarkerDetailCard(
    marker: MapMarkerData,
    onClose: () -> Unit,
    onVerify: () -> Unit,
) {
    val statusColor = when (marker.type) {
        MarkerType.HEALTHY -> EmeraldHealthy
        MarkerType.WARNING -> WarningAmber
        MarkerType.ALERT -> AlertCrimson
        MarkerType.QUEST -> QuestPurple
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.WaterDrop,
                    contentDescription = "Observation",
                    tint = statusColor,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        marker.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Logged by ${marker.author} • ${marker.timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("pH Balance", marker.ph.toString())
                MetricChip("Turbidity", marker.turbidity)
                MetricChip("Microplastics", marker.microplasticRisk)
            }

            Spacer(Modifier.height(14.dp))

            // Verify action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(XpGradientStart, XpGradientEnd),
                        ),
                    )
                    .clickable(onClick = onVerify)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "⚡ Verify Observation with AI Scan (+100 XP)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
