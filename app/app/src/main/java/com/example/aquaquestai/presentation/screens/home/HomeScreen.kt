package com.example.aquaquestai.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.R
import com.example.aquaquestai.data.model.EuropeanCity
import com.example.aquaquestai.data.registry.EuropeanCityRegistry
import com.example.aquaquestai.presentation.AquaQuestViewModel
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeScreen(
    viewModel: AquaQuestViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToQuests: () -> Unit,
    onNavigateToStories: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAquaHealth: () -> Unit,
    onNavigateToResearchNews: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val bgColor = Color(0xFFF4F8F9) // Light theme clean background

    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // ── 1. Top Navigation Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Logo Icon (Water Drop + Leaf badge)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00ACC1), Color(0xFF00897B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "AquaQuest Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AquaQuest ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0E3A52),
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00A86B),
                                    fontSize = 20.sp
                                )
                            )
                        }
                        Text(
                            text = "Cleaner Water • Healthier Communities • A Safer Planet",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF5A7B8C),
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Bell Button with Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2ECEF), CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1E3A4C),
                            modifier = Modifier.size(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                    }

                    // User Profile Avatar with Online Status (Matching Reference Design)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val context = LocalContext.current
                        val avatarRequest = remember {
                            ImageRequest.Builder(context)
                                .data("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80")
                                .crossfade(true)
                                .build()
                        }
                        AsyncImage(
                            model = avatarRequest,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF00ACC1), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 2. Hero Card with Photorealistic Background & Floating Camera Button ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(380.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                // Scenic Gradient Fallback + Hero River Landscape Background Image with Blue Water Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0288D1), Color(0xFF00ACC1), Color(0xFF004D40))
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_river_landscape),
                        contentDescription = "Scenic River Landscape",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Vibrant Sky-Blue & Cyan Water Overlay Effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0277BD).copy(alpha = 0.35f),
                                        Color(0xFF00ACC1).copy(alpha = 0.20f),
                                        Color(0xFF004D40).copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                }

                // Gradient Overlay for Title Contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.50f)
                                )
                            )
                        )
                )

                // Bottom Smooth Wave Cut-Out Shape (Matches Screen Background)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val wavePath = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height * 0.82f)
                        cubicTo(
                            size.width * 0.25f, size.height * 0.72f,
                            size.width * 0.75f, size.height * 0.95f,
                            size.width, size.height * 0.80f
                        )
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = wavePath, color = bgColor)
                }

                // Hand-Drawn White Curved Arrow Mark with Arrowhead pointing to Camera Button
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val startX = size.width * 0.78f
                    val startY = size.height * 0.54f
                    val endX = size.width * 0.60f
                    val endY = size.height * 0.72f

                    // Smooth Curved Path
                    val arrowPath = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            size.width * 0.76f, size.height * 0.64f,
                            size.width * 0.68f, size.height * 0.70f,
                            endX, endY
                        )
                    }

                    drawPath(
                        path = arrowPath,
                        color = Color.White.copy(alpha = 0.95f),
                        style = Stroke(
                            width = 4.5f,
                            cap = StrokeCap.Round
                        )
                    )

                    // Arrowhead pointing towards camera button (endX, endY)
                    val arrowhead = Path().apply {
                        moveTo(endX, endY)
                        lineTo(endX + 16f, endY - 14f)
                        moveTo(endX, endY)
                        lineTo(endX + 18f, endY + 2f)
                    }
                    drawPath(
                        path = arrowhead,
                        color = Color.White.copy(alpha = 0.95f),
                        style = Stroke(
                            width = 4.5f,
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Content Overlay Inside Hero Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Tag: "Citizen Science"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Citizen Science",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Headline & Subtitle
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(0.64f)
                        ) {
                            Text(
                                text = "See it.\nCapture it.",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    lineHeight = 34.sp
                                )
                            )
                            Text(
                                text = "Make an impact.",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E676),
                                    fontSize = 30.sp,
                                    lineHeight = 34.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Take a photo of a stream or river. Our AI will analyze the water, detect potential issues and show you what it means for people, pets and wildlife.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }

                    // Arrow annotation text right
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 6.dp, bottom = 64.dp)
                    ) {
                        Text(
                            text = "Your photo\nhelps build a\nhealthier future",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }

                // ── Enhanced Big Central Floating Camera Button ──
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .size(146.dp)
                        .clickable { onNavigateToScan() },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Translucent Glow Aura Ring
                    Box(
                        modifier = Modifier
                            .size(142.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF80DEEA).copy(alpha = 0.75f),
                                        Color(0xFF00E5FF).copy(alpha = 0.40f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Outer White Translucent Ring
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.45f))
                    )

                    // Main Vibrant Gradient Camera Circle Button
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF00B4D8),
                                        Color(0xFF00E5FF),
                                        Color(0xFF00897B)
                                    )
                                )
                            )
                            .border(3.5.dp, Color.White, CircleShape)
                            .shadow(14.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take a Photo",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Take a Photo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Analyze Water Quality",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── 3. 4 Feature Action Cards Grid (2x2) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StyledFeatureCard(
                        title = "Explore Map",
                        subtitle = "View observations\nnear you",
                        icon = Icons.Default.LocationOn,
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToMap() }
                    )
                    StyledFeatureCard(
                        title = "Water Data",
                        subtitle = "Live & historical\nmeasurements",
                        icon = Icons.Outlined.BarChart,
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF43A047),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAquaHealth() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StyledFeatureCard(
                        title = "Research & News",
                        subtitle = "Latest studies\nand reports",
                        icon = Icons.Outlined.FindInPage,
                        iconBg = Color(0xFFF3E5F5),
                        iconTint = Color(0xFF8E24AA),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToResearchNews() }
                    )
                    StyledFeatureCard(
                        title = "Community",
                        subtitle = "Join quests &\nverify observations",
                        icon = Icons.Default.Group,
                        iconBg = Color(0xFFFFF8E1),
                        iconTint = Color(0xFFFB8C00),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCommunity() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ── 4. Featured European Pilot Cities Section ──
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00A86B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Featured European Pilot Cities",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0E3A52),
                                fontSize = 16.sp
                            )
                        )
                    }
                    TextButton(onClick = { onNavigateToMap() }) {
                        Text(
                            text = "View all →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF00897B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val pilotCities = EuropeanCityRegistry.PILOT_CITIES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pilotCities.forEach { city ->
                        PilotCityPhotoCard(
                            city = city,
                            isSelected = viewModel.selectedCity.id == city.id,
                            onClick = {
                                viewModel.selectCity(city)
                                onNavigateToMap()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ── 5. One Health Banner ("Together for healthier waters") ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00695C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Together for healthier waters",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF004D40),
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "Your observations contribute to One Health — for people, pets and wildlife.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF00695C),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }

                    // Icons on right (Person, Dog/Pets, Leaf with water wave underline)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(18.dp))
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(18.dp))
                        }
                        // Wave stroke canvas
                        Canvas(modifier = Modifier.size(width = 54.dp, height = 8.dp)) {
                            val wPath = Path().apply {
                                moveTo(0f, size.height * 0.5f)
                                cubicTo(
                                    size.width * 0.25f, 0f,
                                    size.width * 0.75f, size.height,
                                    size.width, size.height * 0.5f
                                )
                            }
                            drawPath(path = wPath, color = Color(0xFF00796B), style = Stroke(width = 3f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ── 6. Recent Activity Section ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0E3A52),
                                fontSize = 16.sp
                            )
                        )
                    }
                    TextButton(onClick = { onNavigateToCommunity() }) {
                        Text(
                            text = "View all →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStories() },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Thumbnail Box with image fallback
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.hero_river_landscape),
                                        contentDescription = "Recent Stream",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = "♥ Analyzed",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "0.8 km away • 2h ago",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF78909C),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = "Riverside Stream – Coimbra",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0E3A52),
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color(0xFF78909C)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Details",
                                    tint = Color(0xFF78909C),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Tags Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusTagItem(text = "Possible algae", dotColor = Color(0xFFF57F17), bgColor = Color(0xFFFFF9C4), textColor = Color(0xFFE65100))
                            StatusTagItem(text = "Floating debris", dotColor = Color(0xFFD84315), bgColor = Color(0xFFFFCCBC), textColor = Color(0xFFBF360C))
                            StatusTagItem(text = "Clear water", dotColor = Color(0xFF00838F), bgColor = Color(0xFFE0F7FA), textColor = Color(0xFF006064))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StyledFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0E3A52),
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        ),
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go",
                    tint = Color(0xFF0E3A52).copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PilotCityPhotoCard(
    city: EuropeanCity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(155.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0F2F1) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF00897B)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scenic Photo Box — loaded from URL via Coil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                if (city.heroImageUrl.isNotBlank()) {
                    val context = LocalContext.current
                    val imageRequest = remember(city.heroImageUrl) {
                        ImageRequest.Builder(context)
                            .data(city.heroImageUrl.trim())
                            .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36")
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "${city.name} – ${city.riverSystem}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = { errorState ->
                            Log.e("CoilImage", "Error loading image for ${city.name} from ${city.heroImageUrl.trim()}", errorState.result.throwable)
                        }
                    )
                } else {
                    // Gradient fallback when no URL is available
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00897B), Color(0xFF00ACC1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = city.countryFlag,
                    fontSize = 16.sp
                )
                Column {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E3A52),
                            fontSize = 13.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTagItem(
    text: String,
    dotColor: Color,
    bgColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
