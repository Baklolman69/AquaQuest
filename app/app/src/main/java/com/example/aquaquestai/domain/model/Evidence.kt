package com.example.aquaquestai.domain.model

enum class EvidenceSourceType(val label: String) {
    USGS_ENVIRONMENTAL("Official Environmental Telemetry"),
    CITIZEN_OBSERVATION("Citizen Science Observation"),
    REMOTE_OBSERVATION("Remote Observation"),
    HISTORICAL_BASELINE("Historical Baseline"),
    BIODIVERSITY_OBSERVATION("Biodiversity Observation"),
    TRUSTED_HEALTH_SOURCE("Trusted Public Health Evidence")
}

enum class CitizenObservationCategory(val label: String, val icon: String) {
    UNUSUAL_WATER_COLOR("Unusual Water Color", "🎨"),
    FOAM("Foam / Scum", "🫧"),
    ALGAE_LIKE_MATERIAL("Algae-like Material", "🌿"),
    WASTE_DEBRIS("Waste / Debris", "🗑️"),
    FISH_WILDLIFE_ISSUE("Fish / Wildlife Issue", "🐟"),
    UNUSUAL_WATER_LEVEL("Unusual Water Level", "📏"),
    VEGETATION_CHANGE("Vegetation Change", "🌱"),
    UNUSUAL_ODOR("Unusual Odor", "👃"),
    BIODIVERSITY("Biodiversity Species", "🦋"),
    OTHER("Other Observation", "🔍")
}

enum class BaselineWindow(val label: String) {
    HOURS_24("24 Hours"),
    DAYS_7("7 Days"),
    DAYS_30("30 Days"),
    DAYS_90("90 Days")
}

data class HistoricalBaseline(
    val parameter: String,
    val baselineValue: Double?,
    val currentValue: Double?,
    val unit: String,
    val timeWindow: BaselineWindow = BaselineWindow.DAYS_7,
    val percentageDeviation: Double? = null,
    val statusText: String
)

data class Evidence(
    val id: String,
    val sourceType: EvidenceSourceType,
    val parameter: String,
    val value: String,
    val unit: String,
    val observationTimestamp: Long,
    val retrievalTimestamp: Long = System.currentTimeMillis(),
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val stationId: String? = null,
    val description: String = "",
    val sourceUrl: String? = null,
    val limitations: String = "",
    val photoUri: String? = null,
    val category: CitizenObservationCategory? = null,
    val observerId: String? = null
)
