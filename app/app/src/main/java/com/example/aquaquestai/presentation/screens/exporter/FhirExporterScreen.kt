package com.example.aquaquestai.presentation.screens.exporter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.data.OneHealthExporter
import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import com.example.aquaquestai.presentation.screens.map.MarkerType
import com.example.aquaquestai.theme.*

@Composable
fun FhirExporterScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = FHIR, 1 = GeoJSON
    val clipboardManager = LocalClipboardManager.current
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Sample pilot markers for demonstration
    val sampleMarkers = remember {
        listOf(
            MapMarkerData(
                id = 1,
                lat = 40.2033,
                lng = -8.4103,
                title = "Mondego Stream Sector (Coimbra, PT)",
                snippet = "Moderate algae & turbidity detected",
                type = MarkerType.HEALTHY,
                ph = 7.2,
                turbidity = "12 NTU",
                microplasticRisk = "Moderate",
                author = "Dr. Maria (Coimbra Scout)",
                timestamp = "Just now"
            ),
            MapMarkerData(
                id = 2,
                lat = 51.0543,
                lng = 3.7174,
                title = "Scheldt Canal Sector (Gent, BE)",
                snippet = "Oil sheen & debris on canal wall",
                type = MarkerType.ALERT,
                ph = 6.8,
                turbidity = "24 NTU",
                microplasticRisk = "High",
                author = "Alex (Gent HydroDefender)",
                timestamp = "10 mins ago"
            )
        )
    }

    val jsonOutput = remember(selectedTab) {
        if (selectedTab == 0) {
            OneHealthExporter.exportHl7FhirObservations(sampleMarkers)
        } else {
            OneHealthExporter.exportGeoJson(sampleMarkers)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "HL7 FHIR & FAIR Exporter",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryWhite
            )
            Text(
                text = "IEEE OneAquaHealth Track 7 — Digital Health Standards Interoperability",
                style = MaterialTheme.typography.bodySmall,
                color = BioCyanPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = OceanCardSurface,
                contentColor = BioCyanPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("HL7 FHIR R4", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("GeoJSON (ISO 19115)", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions & Metadata Box
            Surface(
                color = OceanCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedTab == 0) "HL7 FHIR Observation Bundle" else "GeoJSON Feature Collection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = if (selectedTab == 0) "LOINC Code 21612-7 | WFD Quality Panels" else "OGC CRS84 | QGIS & OpenStreetMap Compatible",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryMuted
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(jsonOutput))
                            snackbarMessage = "Copied to clipboard!"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BioCyanPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Code Display Area
            Surface(
                color = OceanDeepCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 500.dp)
                    .border(1.dp, BioCyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                SelectionContainer {
                    Text(
                        text = jsonOutput,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = EmeraldHealthy,
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
