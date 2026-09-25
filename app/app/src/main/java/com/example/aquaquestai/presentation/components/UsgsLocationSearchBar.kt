package com.example.aquaquestai.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.data.UsgsWaterHealthReport
import com.example.aquaquestai.data.UsgsWaterService
import com.example.aquaquestai.theme.*
import kotlinx.coroutines.launch

@Composable
fun UsgsLocationSearchBar(
    onReportFetched: (UsgsWaterHealthReport) -> Unit,
    onCardClick: (UsgsWaterHealthReport) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeReport by remember { mutableStateOf<UsgsWaterHealthReport?>(null) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Search Input Bar (Fresh Eco Light Mode)
        Surface(
            color = LightSurface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Location",
                    tint = LightPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search city or station (e.g. Austin, Denver, Hudson)...",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextMuted
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = LightTextPrimary,
                        unfocusedTextColor = LightTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchQuery.isNotBlank()) {
                            keyboardController?.hide()
                            isLoading = true
                            scope.launch {
                                val report = UsgsWaterService.fetchWaterDataByLocation(searchQuery)
                                activeReport = report
                                isLoading = false
                                onReportFetched(report)
                            }
                        }
                    }),
                    modifier = Modifier.weight(1f)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = LightPrimary,
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        activeReport = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = LightTextMuted)
                    }
                }
            }
        }

        // Live Clickable USGS Water Result Card (Light Mode) or Explicit Unavailable Card
        AnimatedVisibility(visible = activeReport != null) {
            activeReport?.let { report ->
                Spacer(modifier = Modifier.height(10.dp))

                if (!report.isDataAvailable) {
                    // Explicit Unavailable State (No Fake Data Generated)
                    Surface(
                        color = LightSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, AlertCrimson.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Data Unavailable",
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "USGS TELEMETRY UNAVAILABLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AlertCrimson
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.explainableSummary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = LightTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Zero synthetic data generated. Try searching a major US station, city, or state (e.g. Austin, Denver, NY, CA).",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextMuted
                            )
                        }
                    }
                } else {
                    // Valid USGS Live Observation Card
                    Surface(
                        color = LightSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onCardClick(report) }
                            .border(1.5.dp, LightPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Water,
                                        contentDescription = "USGS Station",
                                        tint = LightPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "USGS LIVE TELEMETRY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = LightPrimary
                                    )
                                }

                                Surface(
                                    color = if (report.oneHealthRiskScore <= 3) EmeraldHealthy.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "STATUS: ${report.environmentalHealthStatus.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (report.oneHealthRiskScore <= 3) EmeraldHealthy else AlertCrimson,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.locationName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LightTextPrimary
                            )
                            Text(
                                text = report.explainableSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Water Metrics Grid (Handling Nullables Safely)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LightMetricTile("Streamflow", report.streamFlowCfs?.let { "$it cfs" } ?: "N/A")
                                LightMetricTile("Temp", report.waterTempCelsius?.let { "$it°C" } ?: "N/A")
                                LightMetricTile("pH", report.ph?.let { "$it" } ?: "N/A")
                                LightMetricTile("Turbidity", report.turbidityNtu?.let { "$it NTU" } ?: "N/A")
                                LightMetricTile("Diss. O2", report.dissolvedOxygenMgL?.let { "$it mg/L" } ?: "N/A")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Call-to-action bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tap for full hydrographic & biological analysis",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LightPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Details",
                                    tint = LightPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LightMetricTile(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LightTextSecondary,
            fontSize = 8.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = LightPrimary,
            fontSize = 11.sp
        )
    }
}
