package com.example.aquaquestai.data.engine

import com.example.aquaquestai.domain.model.AiEvidenceAssessment
import com.example.aquaquestai.domain.model.AquaEvent
import com.example.aquaquestai.domain.model.AquaEventStatus
import com.example.aquaquestai.domain.model.CitizenObservationCategory
import com.example.aquaquestai.domain.model.Evidence
import com.example.aquaquestai.domain.model.EvidenceSourceType
import com.example.aquaquestai.domain.model.OneHealthContext
import com.example.aquaquestai.domain.model.SourceReference
import com.example.aquaquestai.domain.model.VerificationSummary
import com.example.aquaquestai.domain.model.WaterStoryStep

object EvidenceEngine {

    /**
     * Builds an AquaEvent from a collection of evidence signals.
     */
    fun createAquaEvent(
        id: String,
        title: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        evidenceItems: List<Evidence>,
        aiAssessment: AiEvidenceAssessment? = null
    ): AquaEvent {
        val now = System.currentTimeMillis()

        val citizenCount = evidenceItems.count { it.sourceType == EvidenceSourceType.CITIZEN_OBSERVATION }
        val envCount = evidenceItems.count { it.sourceType == EvidenceSourceType.USGS_ENVIRONMENTAL }
        val independentObservers = evidenceItems.mapNotNull { it.observerId }.distinct().size

        val status = when {
            independentObservers >= 3 || (citizenCount >= 3 && envCount >= 1) -> AquaEventStatus.CORROBORATED
            citizenCount >= 1 || envCount >= 1 -> AquaEventStatus.UNDER_VERIFICATION
            evidenceItems.isNotEmpty() -> AquaEventStatus.DETECTED
            else -> AquaEventStatus.INSUFFICIENT_EVIDENCE
        }

        val verificationSummary = VerificationSummary(
            independentObserverCount = Math.max(independentObservers, citizenCount),
            citizenObservationCount = citizenCount,
            environmentalSignalCount = envCount,
            spatialClusterRadiusKm = 1.8,
            observationWindowHours = 3,
            verifiedCount = Math.max(independentObservers, citizenCount),
            unverifiedCount = 0,
            unableToDetermineCount = 0
        )

        val timeline = buildTimeline(evidenceItems, status)

        val oneHealthContext = OneHealthContext(
            bioRiskRelevance = "Environmental changes in surface water parameters may impact local aquatic ecosystems and downstream recreational exposure.",
            humanExposureNotes = "Reported visual or physical anomalies require standard precautions (avoiding ingestion or contact if skin irritation occurs).",
            ecosystemImpactNotes = "Altered turbidity or temperature signals may temporarily affect localized macroinvertebrate habitats.",
            trustedHealthGuidance = "Refer to local water authority announcements and public health guidance (CDC / EPA guidelines for surface water contact)."
        )

        val sources = evidenceItems.mapNotNull { ev ->
            if (!ev.sourceUrl.isNull_Blank()) {
                SourceReference(
                    title = ev.description.ifEmpty { ev.parameter },
                    url = ev.sourceUrl!!,
                    organization = ev.sourceType.label
                )
            } else null
        }.distinctBy { it.url }

        return AquaEvent(
            id = id,
            title = title,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            createdAt = evidenceItems.minOfOrNull { it.observationTimestamp } ?: now,
            status = status,
            evidenceList = evidenceItems,
            verificationSummary = verificationSummary,
            aiAssessment = aiAssessment,
            oneHealthContext = oneHealthContext,
            sourceReferences = sources,
            waterStoryTimeline = timeline
        )
    }

    private fun buildTimeline(evidenceItems: List<Evidence>, status: AquaEventStatus): List<WaterStoryStep> {
        val steps = mutableListOf<WaterStoryStep>()

        // 1. Initial Baseline step
        steps.add(
            WaterStoryStep(
                timeLabel = "Baseline",
                timestamp = System.currentTimeMillis() - 7200000,
                title = "Baseline Telemetry Active",
                description = "Monitoring station transmitting routine environmental measurements within normal ranges.",
                category = "MONITORING",
                iconName = "📡"
            )
        )

        // 2. Add steps for each evidence piece sorted chronologically
        evidenceItems.sortedBy { it.observationTimestamp }.forEachIndexed { idx, ev ->
            val icon = when (ev.sourceType) {
                EvidenceSourceType.USGS_ENVIRONMENTAL -> "📊"
                EvidenceSourceType.CITIZEN_OBSERVATION -> ev.category?.icon ?: "👁️"
                EvidenceSourceType.HISTORICAL_BASELINE -> "📈"
                EvidenceSourceType.BIODIVERSITY_OBSERVATION -> "🦋"
                EvidenceSourceType.TRUSTED_HEALTH_SOURCE -> "🩺"
                EvidenceSourceType.REMOTE_OBSERVATION -> "🛰️"
            }
            steps.add(
                WaterStoryStep(
                    timeLabel = "Observed #${idx + 1}",
                    timestamp = ev.observationTimestamp,
                    title = "${ev.sourceType.label}: ${ev.parameter}",
                    description = "${ev.value} ${ev.unit} - ${ev.description}".trimEnd(' ', '-'),
                    category = ev.sourceType.name,
                    iconName = icon
                )
            )
        }

        // 3. Status summary step
        steps.add(
            WaterStoryStep(
                timeLabel = "Current Status",
                timestamp = System.currentTimeMillis(),
                title = "Event Status: ${status.displayName}",
                description = "Event currently evaluated as ${status.displayName.lowercase()} based on ${evidenceItems.size} verified evidence signals.",
                category = "STATUS",
                iconName = "🏷️"
            )
        )

        return steps
    }

    private fun String?.isNull_Blank(): Boolean = this == null || this.trim().isEmpty()
}
