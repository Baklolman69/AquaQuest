package com.example.aquaquestai.data.local

data class QuestEntity(
    val id: String,
    val title: String,
    val description: String,
    val cityId: String,
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Double = 2000.0, // 2km geofence
    val xpReward: Int = 100,
    val isCompleted: Boolean = false,
    val consensusConfidencePercent: Int = 30, // 30% initial ➔ 85% dual ➔ 98% multi
    val anomalyType: String, // Algal Bloom, Turbidity Surge, Micro-Trash, Oil Sheen
    val createdAtTimestamp: Long
)
