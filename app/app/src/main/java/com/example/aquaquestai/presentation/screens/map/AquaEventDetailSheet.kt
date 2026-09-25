package com.example.aquaquestai.presentation.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.domain.model.AquaEvent
import com.example.aquaquestai.domain.model.AquaEventStatus
import com.example.aquaquestai.presentation.components.WaterStoryTimelineComponent
import com.example.aquaquestai.theme.AlertCrimson
import com.example.aquaquestai.theme.EmeraldHealthy
import com.example.aquaquestai.theme.WarningAmber

@Composable
fun AquaEventDetailSheet(
    event: AquaEvent,
    onDismiss: () -> Unit,
    onVerifyClick: (String, String) -> Unit, // eventId, voteType ("OBSERVED", "NOT_OBSERVED", "UNABLE_TO_DETERMINE")
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val surfaceBg = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantBg = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderOutline = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    val statusColor = when (event.status) {
        AquaEventStatus.CORROBORATED -> EmeraldHealthy
        AquaEventStatus.UNDER_VERIFICATION -> WarningAmber
        AquaEventStatus.DETECTED -> AlertCrimson
        AquaEventStatus.RESOLVED -> EmeraldHealthy
        AquaEventStatus.INSUFFICIENT_EVIDENCE -> Color.Gray
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(surfaceBg)
            .border(1.dp, borderOutline, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Header: Title & Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.status.displayName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📍 ${event.locationName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceMuted
                        )
                    }

                    Text(
                        text = event.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = onSurfaceColor
                    )
                }
            }

            // Real Corroboration Stats Bar (No fake confidence percentages!)
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceVariantBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${event.verificationSummary.independentObserverCount}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(text = "Observers", fontSize = 11.sp, color = onSurfaceMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${event.verificationSummary.citizenObservationCount}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(text = "Citizen Obs", fontSize = 11.sp, color = onSurfaceMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${event.verificationSummary.environmentalSignalCount}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(text = "Env Signals", fontSize = 11.sp, color = onSurfaceMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${event.verificationSummary.spatialClusterRadiusKm} km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Text(text = "Cluster", fontSize = 11.sp, color = onSurfaceMuted)
                    }
                }
            }

            // AI Evidence Assistant Block
            event.aiAssessment?.let { ai ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧠 AquaQuest AI Evidence Interpretation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                        Text(
                            text = ai.possibleInterpretation,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = ai.observationSummary,
                            fontSize = 12.sp,
                            color = onSurfaceMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Supporting Evidence:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldHealthy)
                        ai.supportingEvidence.forEach { supp ->
                            Text(text = "• $supp", fontSize = 11.sp, color = onSurfaceColor)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Limitations & Non-Diagnostic Constraints:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                        ai.limitations.forEach { limit ->
                            Text(text = "⚠️ $limit", fontSize = 11.sp, color = onSurfaceMuted)
                        }
                    }
                }
            }

            // Water Story Visual Timeline
            if (event.waterStoryTimeline.isNotEmpty()) {
                WaterStoryTimelineComponent(
                    steps = event.waterStoryTimeline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Evidence Provenance List
            Text(
                text = "📊 Verified Evidence Signals (${event.evidenceList.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            event.evidenceList.forEach { ev ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceVariantBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f))
                        ) {
                            Text(text = ev.category?.icon ?: "📌", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${ev.sourceType.label}: ${ev.parameter}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "${ev.value} ${ev.unit} • ${ev.description}".trimEnd(' ', '•'),
                                fontSize = 12.sp,
                                color = onSurfaceMuted
                            )
                            if (ev.limitations.isNotEmpty()) {
                                Text(
                                    text = "Limitations: ${ev.limitations}",
                                    fontSize = 10.sp,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }

            // Independent Community Verification CTA
            Text(
                text = "🗳️ Submit Independent Ground Verification",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            Text(
                text = "Have you observed this condition near this stream section?",
                fontSize = 12.sp,
                color = onSurfaceMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onVerifyClick(event.id, "OBSERVED") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldHealthy),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Observed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onVerifyClick(event.id, "NOT_OBSERVED") },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Not Seen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onVerifyClick(event.id, "UNABLE_TO_DETERMINE") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Uncertain", fontSize = 11.sp, color = onSurfaceColor)
                }
            }
        }
    }
}
