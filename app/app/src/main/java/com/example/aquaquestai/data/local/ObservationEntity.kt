package com.example.aquaquestai.data.local

data class ObservationEntity(
    val id: String,
    val cityId: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val imageUrl: String?,
    val waterClarityIndex: Int, // 1 to 10
    val wfdStatus: String, // High, Good, Moderate, Poor, Bad
    val humanHealthRiskScore: Int, // 1 to 10
    val petAnimalRiskScore: Int, // 1 to 10
    val aquaticEcosystemRiskScore: Int, // 1 to 10
    val overallRiskScore: Int, // 1 to 10
    val explainableSummary: String,
    val catalogueOfMeasuresRecommendation: String,
    val verifiedCount: Int = 1,
    val isAnomalyFlagged: Boolean = false,
    val isSyncedToFhir: Boolean = false
)
