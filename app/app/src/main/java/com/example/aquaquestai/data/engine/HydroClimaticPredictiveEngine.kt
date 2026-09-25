package com.example.aquaquestai.data.engine

import com.example.aquaquestai.data.UsgsWaterHealthReport
import kotlin.math.roundToInt

enum class HydroTrendCategory {
    STABLE_BASELINE,
    RISING_SURGE_HAZARD,
    ALGAE_BLOOM_ESCALATION,
    HYPOXIC_CRASH_WARNING,
    MULTI_HAZARD_ALERT
}

data class HazardRiskIndex(
    val name: String,
    val score: Int, // 0 to 100
    val levelStr: String, // "Low", "Moderate", "High", "Critical"
    val riskDescription: String,
    val primaryFactor: String
)

data class HydroClimaticForecastResult(
    val overallRiskScore: Int, // 1 to 100
    val trendCategory: HydroTrendCategory,
    val forecastTitle: String,
    val forecastSummary: String,
    val flashSurgeIndex: HazardRiskIndex,
    val algaeBloomRiskIndex: HazardRiskIndex,
    val hypoxiaStressIndex: HazardRiskIndex,
    val euWfdMeasures: List<String>,
    val forecastConfidence: String,
    val mitigationDirective: String
)

/**
 * HydroClimaticPredictiveEngine
 * Advanced predictive algorithm modeling hydro-climatic multi-hazards, 48-hour stream trends,
 * cyanobacteria bloom windows, hypoxic crash risks, and EU WFD (2000/60/EC) mitigation measures.
 */
object HydroClimaticPredictiveEngine {

    fun generate48HourForecast(report: UsgsWaterHealthReport): HydroClimaticForecastResult {
        if (!report.isDataAvailable) {
            return HydroClimaticForecastResult(
                overallRiskScore = 0,
                trendCategory = HydroTrendCategory.STABLE_BASELINE,
                forecastTitle = "Insufficient Data",
                forecastSummary = "Live telemetry unavailable to calculate predictive hydro-climatic risks.",
                flashSurgeIndex = HazardRiskIndex("Flash Flood Surge", 0, "Low", "Insufficient flow metrics", "N/A"),
                algaeBloomRiskIndex = HazardRiskIndex("Harmful Algal Bloom", 0, "Low", "Insufficient thermal metrics", "N/A"),
                hypoxiaStressIndex = HazardRiskIndex("Hypoxia Risk", 0, "Low", "Insufficient oxygen metrics", "N/A"),
                euWfdMeasures = listOf("Maintain basic catchment monitoring"),
                forecastConfidence = "Low",
                mitigationDirective = "Verify station telemetry connection."
            )
        }

        // ── 1. Flash Flood & Hydraulic Surge Acceleration Index (0..100) ──
        val flowCfs = report.streamFlowCfs ?: 0.0
        val gaugeFt = report.gaugeHeightFt ?: 0.0
        
        // Base surge calculation using stage height ratio & flow volume
        val flowSurgeScore = when {
            flowCfs > 1500.0 || gaugeFt > 12.0 -> 88
            flowCfs > 800.0 || gaugeFt > 8.0 -> 68
            flowCfs > 400.0 || gaugeFt > 5.5 -> 42
            flowCfs > 150.0 || gaugeFt > 3.5 -> 20
            else -> 10
        }

        val surgeLevel = when {
            flowSurgeScore >= 75 -> "Critical"
            flowSurgeScore >= 50 -> "High"
            flowSurgeScore >= 25 -> "Moderate"
            else -> "Low"
        }

        val surgeFactor = if (gaugeFt > 0) "Stage Height: ${String.format("%.1f", gaugeFt)} ft" else "Discharge: ${String.format("%.0f", flowCfs)} cfs"
        val surgeIndex = HazardRiskIndex(
            name = "Flash Flood & Hydraulic Surge",
            score = flowSurgeScore,
            levelStr = surgeLevel,
            riskDescription = if (flowSurgeScore >= 50) "High volumetric discharge rate; potential bank inundation in 24–48h." else "Streamflow volume within normal seasonal limits.",
            primaryFactor = surgeFactor
        )

        // ── 2. Harmful Algal Bloom (HAB) Thermal-Nutrient Early Warning (0..100) ──
        val waterTempC = report.waterTempCelsius ?: 18.0
        val phVal = report.ph ?: 7.2
        val turbNtu = report.turbidityNtu ?: 2.0
        val doMgL = report.dissolvedOxygenMgL ?: 8.0

        var habScore = 10
        if (waterTempC >= 24.0) habScore += 35
        else if (waterTempC >= 20.0) habScore += 20

        if (phVal >= 8.2) habScore += 30
        else if (phVal >= 7.8) habScore += 15

        if (turbNtu in 3.0..15.0) habScore += 15 // Organic particulate window
        if (doMgL < 6.0) habScore += 10 // Eutrophic respiration signal

        val habFinalScore = habScore.coerceIn(0, 100)
        val habLevel = when {
            habFinalScore >= 70 -> "Critical"
            habFinalScore >= 45 -> "High"
            habFinalScore >= 25 -> "Moderate"
            else -> "Low"
        }

        val habFactor = "Temp: ${String.format("%.1f", waterTempC)}°C | pH: ${String.format("%.1f", phVal)}"
        val habIndex = HazardRiskIndex(
            name = "Harmful Algal Bloom (HAB)",
            score = habFinalScore,
            levelStr = habLevel,
            riskDescription = if (habFinalScore >= 45) "Thermal-alkaline conditions promote cyanobacteria proliferation within 48h." else "Low algae bloom development probability.",
            primaryFactor = habFactor
        )

        // ── 3. Hypoxia & Aquatic Fauna Stress Index (0..100) ──
        var hypoxiaScore = 10
        if (doMgL < 4.0) hypoxiaScore += 70
        else if (doMgL < 5.5) hypoxiaScore += 45
        else if (doMgL < 6.5) hypoxiaScore += 25

        if (waterTempC > 25.0) hypoxiaScore += 20 // Reduced gas solubility
        
        val hypoxiaFinalScore = hypoxiaScore.coerceIn(0, 100)
        val hypoxiaLevel = when {
            hypoxiaFinalScore >= 70 -> "Critical"
            hypoxiaFinalScore >= 45 -> "High"
            hypoxiaFinalScore >= 25 -> "Moderate"
            else -> "Low"
        }

        val hypoxiaFactor = "DO Concentration: ${String.format("%.1f", doMgL)} mg/L"
        val hypoxiaIndex = HazardRiskIndex(
            name = "Hypoxia & Habitat Stress",
            score = hypoxiaFinalScore,
            levelStr = hypoxiaLevel,
            riskDescription = if (hypoxiaFinalScore >= 45) "Dissolved oxygen depletion detected; imminent stress on macroinvertebrates and fish." else "Dissolved oxygen adequate for stream fauna.",
            primaryFactor = hypoxiaFactor
        )

        // ── 4. Aggregate Overall 48-Hour Forecast ──
        val overallRiskScore = maxOf(flowSurgeScore, habFinalScore, hypoxiaFinalScore)

        val trendCategory = when {
            flowSurgeScore >= 60 && habFinalScore >= 50 -> HydroTrendCategory.MULTI_HAZARD_ALERT
            flowSurgeScore >= 55 -> HydroTrendCategory.RISING_SURGE_HAZARD
            habFinalScore >= 50 -> HydroTrendCategory.ALGAE_BLOOM_ESCALATION
            hypoxiaFinalScore >= 50 -> HydroTrendCategory.HYPOXIC_CRASH_WARNING
            else -> HydroTrendCategory.STABLE_BASELINE
        }

        val forecastTitle = when (trendCategory) {
            HydroTrendCategory.MULTI_HAZARD_ALERT -> "⚠️ Multi-Hazard Hydro-Climatic Warning"
            HydroTrendCategory.RISING_SURGE_HAZARD -> "🌊 48H Surge & Hydraulic Risk Alert"
            HydroTrendCategory.ALGAE_BLOOM_ESCALATION -> "🦠 Cyanobacteria / Algae Growth Escalation"
            HydroTrendCategory.HYPOXIC_CRASH_WARNING -> "🫁 Aquatic Hypoxia Risk Warning"
            HydroTrendCategory.STABLE_BASELINE -> "🌿 Stable Catchment Hydro-Baseline"
        }

        val forecastSummary = when (trendCategory) {
            HydroTrendCategory.MULTI_HAZARD_ALERT -> "Elevated streamflow and thermal-alkaline readings indicate concurrent flash surge and cyanobacteria bloom risks over the next 48 hours."
            HydroTrendCategory.RISING_SURGE_HAZARD -> "High volumetric discharge and gauge height suggest rising stream velocity and localized bank overflow risk."
            HydroTrendCategory.ALGAE_BLOOM_ESCALATION -> "Warm water temperatures combined with alkaline pH create optimal micro-environmental conditions for algal biomass accumulation."
            HydroTrendCategory.HYPOXIC_CRASH_WARNING -> "Depressed dissolved oxygen levels risk triggering biological stress and oxygen crash for local macroinvertebrates."
            HydroTrendCategory.STABLE_BASELINE -> "Hydro-climatic indicators remain within balanced baseline parameters. Low 48-hour hazard probability."
        }

        // EU Water Framework Directive (WFD 2000/60/EC) Catalogue of Measures
        val measures = mutableListOf<String>()
        if (habFinalScore >= 40) {
            measures.add("EU WFD Measure #14: Establish vegetated riparian buffer zone to shade stream and suppress cyanobacteria proliferation")
        }
        if (flowSurgeScore >= 40) {
            measures.add("EU WFD Measure #22: Inspect downstream retention basins and activate stream bank erosion shields")
        }
        if (hypoxiaFinalScore >= 40) {
            measures.add("EU WFD Measure #08: Enhance stream riffle aeration structures to boost dissolved oxygen exchange")
        }
        if (measures.isEmpty()) {
            measures.add("EU WFD Measure #01: Maintain protected riparian forest canopy and continuous automated sensor telemetry")
        }

        val confidence = if (report.ph != null && report.dissolvedOxygenMgL != null && report.streamFlowCfs != null) "High (Full Telemetry)" else "Moderate (Partial Telemetry)"

        val directive = when {
            overallRiskScore >= 65 -> "PROACTIVE ACTION: Notify river basin managers and issue citizen science verification quest."
            overallRiskScore >= 40 -> "ADVISORY: Monitor sensor telemetry twice daily for thermal or stage shifts."
            else -> "ROUTINE: Standard baseline monitoring active."
        }

        return HydroClimaticForecastResult(
            overallRiskScore = overallRiskScore,
            trendCategory = trendCategory,
            forecastTitle = forecastTitle,
            forecastSummary = forecastSummary,
            flashSurgeIndex = surgeIndex,
            algaeBloomRiskIndex = habIndex,
            hypoxiaStressIndex = hypoxiaIndex,
            euWfdMeasures = measures,
            forecastConfidence = confidence,
            mitigationDirective = directive
        )
    }
}
