package com.example.aquaquestai.data.model

data class EuropeanCity(
    val id: String,
    val name: String,
    val country: String,
    val countryFlag: String,
    val riverSystem: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val primaryEcologicalStressor: String,
    val baselineWfdStatus: String, // e.g. "Good", "Moderate", "Poor"
    val defaultZoom: Float = 13f,
    val heroImageUrl: String = ""
)
