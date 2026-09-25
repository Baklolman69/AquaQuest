package com.example.aquaquestai.presentation.screens.map

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.data.OneHealthExporter
import com.example.aquaquestai.data.UsgsWaterHealthReport
import com.example.aquaquestai.data.WaterHealthEngine
import com.example.aquaquestai.data.engine.HydroClimaticPredictiveEngine
import com.example.aquaquestai.data.engine.HydroClimaticForecastResult
import com.example.aquaquestai.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsgsWaterDetailScreen(
    report: UsgsWaterHealthReport,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    // Evaluate report via WaterHealthEngine
    val assessment = remember(report) {
        WaterHealthEngine.evaluateReport(report)
    }

    // Evaluate 48-hour hydro-climatic forecast via HydroClimaticPredictiveEngine
    val forecast = remember(report) {
        HydroClimaticPredictiveEngine.generate48HourForecast(report)
    }

    // Convert USGS report into standard MapMarkerData for FHIR exporter
    val markerData = remember(report) {
        MapMarkerData(
            id = 1001,
            lat = report.latitude ?: 38.9072,
            lng = report.longitude ?: -77.0369,
            title = report.locationName,
            snippet = "USGS Station ${report.siteId} • Flow: ${report.streamFlowCfs?.let { "$it cfs" } ?: "N/A"}",
            type = if (report.oneHealthRiskScore <= 3) MarkerType.HEALTHY else MarkerType.WARNING,
            ph = report.ph ?: 7.0,
            turbidity = report.turbidityNtu?.let { "$it NTU" } ?: "N/A",
            microplasticRisk = report.dissolvedOxygenMgL?.let { "DO: $it mg/L" } ?: "DO: N/A",
            author = report.providerName,
            timestamp = report.observedTimestampStr ?: report.retrievedTimestampStr
        )
    }

    val fhirJson = remember(report) {
        OneHealthExporter.exportHl7FhirObservations(listOf(markerData))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "USGS Stream Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LightTextPrimary
                        )
                        Text(
                            text = "Station ID: ${report.siteId} • ${report.countyName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Header Banner & Data Provenance ──
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Water,
                                        contentDescription = "Water Data",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = report.providerName.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "STATUS: ${assessment.overallStatus.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (assessment.riskSignalScore <= 3) EmeraldHealthy else AlertCrimson,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = report.locationName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📍 ${report.countyName}, ${report.stateCode} (${report.hucBasinCode}) • Station ${report.siteId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Observed: ${report.observedTimestampStr ?: "Real-Time"} • Retrieved: ${report.retrievedTimestampStr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // ── 2. Data Confidence & Scientific Limitations Banner ──
            Surface(
                color = WarningAmber.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Limitations Banner",
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DATA CONFIDENCE: ${assessment.confidenceLevel.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = WarningAmber
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = assessment.limitationsBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextPrimary
                        )
                    }
                }
            }

            // ── 3. AquaQuest AI Assessment & Risk Signal Engine ──
            Surface(
                color = LightBlueTint,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, LightCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LightPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AquaQuest AI",
                                    tint = LightPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🧠 AquaQuest AI Assessment",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LightPrimary
                                )
                                Text(
                                    text = "Explainable environmental risk signal analysis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightTextSecondary
                                )
                            }
                        }

                        Surface(
                            color = LightPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "RISK SIGNAL: ${assessment.riskSignalScore}/10",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LightPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = assessment.riskSignalExplanation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = LightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Indicator Evidence Breakdown Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        assessment.evidenceList.forEach { ev ->
                            EvidenceRow(evidence = ev)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recommended Action
                    Surface(
                        color = LightGreenTint,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = assessment.recommendedActions.firstOrNull() ?: "Continue automated sensor monitoring",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = EmeraldHealthy
                            )
                        }
                    }
                }
            }

            // ── 48-Hour Hydro-Climatic Predictive Forecast Card ──
            HydroClimaticForecastCard(forecast = forecast)

            // ── 4. Hydrological Flow Telemetry (Observed Data) ──
            Text(
                text = "🌊 Water Flow & Levels (Observed Data)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VibrantMetricCard(
                    icon = Icons.Default.Water,
                    title = "Water Flow Speed",
                    value = report.streamFlowCfs?.let { "$it cfs" } ?: "N/A",
                    subtitle = report.streamFlowM3s?.let { "($it m³/s)" } ?: "(N/A)",
                    badgeText = "Flow relative to baseline",
                    cardBg = LightBlueTint,
                    accentColor = LightPrimary,
                    modifier = Modifier.weight(1f)
                )
                VibrantMetricCard(
                    icon = Icons.Default.Height,
                    title = "River Water Level",
                    value = report.gaugeHeightFt?.let { "$it ft" } ?: "N/A",
                    subtitle = "Stage Height",
                    badgeText = "Stage Level",
                    cardBg = LightPurpleTint,
                    accentColor = QuestPurple,
                    modifier = Modifier.weight(1f)
                )
                VibrantMetricCard(
                    icon = Icons.Default.Thermostat,
                    title = "Water Temperature",
                    value = report.waterTempCelsius?.let { "$it°C" } ?: "N/A",
                    subtitle = report.waterTempFahrenheit?.let { "($it°F)" } ?: "(N/A)",
                    badgeText = "Thermal Reading",
                    cardBg = LightGreenTint,
                    accentColor = EmeraldHealthy,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── 5. Chemical & Physical Metrics (Observed Data) ──
            Text(
                text = "🧪 Chemical & Physical Telemetry (Observed Data)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )

            Surface(
                color = LightSurface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SimpleChemTile(
                            icon = "💧",
                            title = "Water Clarity",
                            valText = report.turbidityNtu?.let { "$it NTU" } ?: "Unavailable",
                            status = "Baseline: < 5.0 NTU"
                        )
                        SimpleChemTile(
                            icon = "🧪",
                            title = "Acidity Level (pH)",
                            valText = report.ph?.let { "$it" } ?: "Unavailable",
                            status = "Configured range: 6.5-8.5"
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = LightBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SimpleChemTile(
                            icon = "🫁",
                            title = "Dissolved Oxygen",
                            valText = report.dissolvedOxygenMgL?.let { "$it mg/L" } ?: "Unavailable",
                            status = report.dissolvedOxygenSaturationPercent?.let { "$it% Saturation" } ?: "DO Saturation: N/A"
                        )
                        SimpleChemTile(
                            icon = "⚡",
                            title = "Specific Conductance",
                            valText = report.specificConductanceUsCm?.let { "$it µS" } ?: "Unavailable",
                            status = report.totalDissolvedSolidsPpm?.let { "TDS: $it ppm" } ?: "TDS: N/A"
                        )
                    }
                }
            }

            // ── 6. IEEE Track 7: HL7 FHIR Observation Exporter ──
            Surface(
                color = LightSurface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "IEEE Track 7: HL7 FHIR Observation Resource",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LightTextPrimary
                            )
                            Text(
                                text = "LOINC Code 21612-7 Water Quality Panel",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextSecondary
                            )
                        }

                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(fhirJson))
                            isCopied = true
                        }) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy FHIR JSON",
                                tint = if (isCopied) EmeraldHealthy else LightPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = LightSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                    ) {
                        SelectionContainer {
                            Text(
                                text = fhirJson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = LightTextPrimary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EvidenceRow(evidence: com.example.aquaquestai.data.IndicatorEvidence) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = evidence.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Text(
                text = evidence.referenceRange,
                style = MaterialTheme.typography.labelSmall,
                color = LightTextMuted,
                fontSize = 9.sp
            )
        }
        Text(
            text = "${evidence.observedValue} (${evidence.statusLabel})",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (evidence.isWithinRange) EmeraldHealthy else AlertCrimson
        )
    }
}

@Composable
private fun VibrantMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    badgeText: String,
    cardBg: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = cardBg,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = LightTextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = LightTextMuted,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpleChemTile(
    icon: String,
    title: String,
    valText: String,
    status: String
) {
    Column(modifier = Modifier.padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = LightTextSecondary)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = valText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LightPrimary)
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = EmeraldHealthy, fontSize = 9.sp)
    }
}

@Composable
private fun HydroClimaticForecastCard(forecast: HydroClimaticForecastResult) {
    val hazardColor = when {
        forecast.overallRiskScore >= 65 -> AlertCrimson
        forecast.overallRiskScore >= 40 -> WarningAmber
        else -> EmeraldHealthy
    }

    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, hazardColor.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(hazardColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔮", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "48-Hour Hydro-Climatic Forecast",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LightTextPrimary
                        )
                        Text(
                            text = "Multi-Hazard & Catchment Risk Model",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextSecondary
                        )
                    }
                }

                Surface(
                    color = hazardColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "RISK SCORE: ${forecast.overallRiskScore}/100",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = hazardColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forecast Banner Category
            Surface(
                color = hazardColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = forecast.forecastTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = hazardColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = forecast.forecastSummary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = LightTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Multi-Hazard Meters
            Text(
                text = "⚡ 48H Multi-Hazard Breakdown",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HazardMeterTile(
                    emoji = "🌊",
                    title = "Flash Surge",
                    score = forecast.flashSurgeIndex.score,
                    levelStr = forecast.flashSurgeIndex.levelStr,
                    factor = forecast.flashSurgeIndex.primaryFactor,
                    modifier = Modifier.weight(1f)
                )
                HazardMeterTile(
                    emoji = "🦠",
                    title = "Algae Bloom",
                    score = forecast.algaeBloomRiskIndex.score,
                    levelStr = forecast.algaeBloomRiskIndex.levelStr,
                    factor = forecast.algaeBloomRiskIndex.primaryFactor,
                    modifier = Modifier.weight(1f)
                )
                HazardMeterTile(
                    emoji = "🫁",
                    title = "Hypoxia Stress",
                    score = forecast.hypoxiaStressIndex.score,
                    levelStr = forecast.hypoxiaStressIndex.levelStr,
                    factor = forecast.hypoxiaStressIndex.primaryFactor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // EU WFD Measures Callout
            Surface(
                color = LightBlueTint,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, LightCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🇪🇺 EU Water Framework Directive (WFD 2000/60/EC) Measures",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LightPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    forecast.euWfdMeasures.forEach { measure ->
                        Text(
                            text = "• $measure",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp, fontSize = 11.sp),
                            color = LightTextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HazardMeterTile(
    emoji: String,
    title: String,
    score: Int,
    levelStr: String,
    factor: String,
    modifier: Modifier = Modifier
) {
    val meterColor = when {
        score >= 65 -> AlertCrimson
        score >= 40 -> WarningAmber
        else -> EmeraldHealthy
    }

    Surface(
        color = LightSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, LightBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightTextPrimary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = meterColor
            )
            Surface(
                color = meterColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = levelStr.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = meterColor,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Text(
                text = factor,
                style = MaterialTheme.typography.labelSmall,
                color = LightTextMuted,
                fontSize = 8.sp,
                maxLines = 1
            )
        }
    }
}
