package com.example.aquaquestai.domain.model

enum class AquaEventStatus(val displayName: String) {
    DETECTED("Detected"),
    UNDER_VERIFICATION("Under Verification"),
    CORROBORATED("Corroborated"),
    RESOLVED("Resolved"),
    INSUFFICIENT_EVIDENCE("Insufficient Evidence")
}

data class VerificationSummary(
    val independentObserverCount: Int = 0,
    val citizenObservationCount: Int = 0,
    val environmentalSignalCount: Int = 0,
    val spatialClusterRadiusKm: Double = 1.8,
    val observationWindowHours: Int = 3,
    val verifiedCount: Int = 0,
    val unverifiedCount: Int = 0,
    val unableToDetermineCount: Int = 0
)

data class SourceReference(
    val title: String,
    val url: String,
    val organization: String
)

data class WaterStoryStep(
    val timeLabel: String,
    val timestamp: Long,
    val title: String,
    val description: String,
    val category: String,
    val iconName: String
)

data class AiEvidenceAssessment(
    val observationSummary: String,
    val possibleInterpretation: String,
    val supportingEvidence: List<String>,
    val missingEvidence: List<String>,
    val limitations: List<String>,
    val suggestedNextStep: String
)

data class OneHealthContext(
    val bioRiskRelevance: String,
    val humanExposureNotes: String,
    val ecosystemImpactNotes: String,
    val trustedHealthGuidance: String
)

data class AquaEvent(
    val id: String,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    val status: AquaEventStatus,
    val evidenceList: List<Evidence> = emptyList(),
    val verificationSummary: VerificationSummary = VerificationSummary(),
    val aiAssessment: AiEvidenceAssessment? = null,
    val oneHealthContext: OneHealthContext? = null,
    val sourceReferences: List<SourceReference> = emptyList(),
    val waterStoryTimeline: List<WaterStoryStep> = emptyList()
)
