package com.example.aquaquestai.data.engine

import com.example.aquaquestai.data.UsgsWaterHealthReport
import org.junit.Assert.*
import org.junit.Test

class HydroClimaticPredictiveEngineTest {

    @Test
    fun testStableBaselineForecast() {
        val report = UsgsWaterHealthReport(
            locationName = "Pristine Stream",
            siteId = "USGS-10000000",
            ph = 7.4,
            turbidityNtu = 2.1,
            dissolvedOxygenMgL = 8.5,
            waterTempCelsius = 16.0,
            streamFlowCfs = 200.0,
            gaugeHeightFt = 3.0,
            isDataAvailable = true
        )

        val forecast = HydroClimaticPredictiveEngine.generate48HourForecast(report)

        assertEquals(HydroTrendCategory.STABLE_BASELINE, forecast.trendCategory)
        assertTrue(forecast.overallRiskScore < 40)
        assertEquals("Low", forecast.flashSurgeIndex.levelStr)
        assertEquals("Low", forecast.algaeBloomRiskIndex.levelStr)
        assertEquals("Low", forecast.hypoxiaStressIndex.levelStr)
        assertTrue(forecast.euWfdMeasures.isNotEmpty())
    }

    @Test
    fun testFlashSurgeHazardForecast() {
        val report = UsgsWaterHealthReport(
            locationName = "Flooding River",
            siteId = "USGS-20000000",
            ph = 7.2,
            turbidityNtu = 12.0,
            dissolvedOxygenMgL = 7.5,
            waterTempCelsius = 18.0,
            streamFlowCfs = 1800.0, // High flow
            gaugeHeightFt = 14.2,   // High stage height
            isDataAvailable = true
        )

        val forecast = HydroClimaticPredictiveEngine.generate48HourForecast(report)

        assertTrue(forecast.trendCategory == HydroTrendCategory.RISING_SURGE_HAZARD || forecast.trendCategory == HydroTrendCategory.MULTI_HAZARD_ALERT)
        assertTrue(forecast.flashSurgeIndex.score >= 75)
        assertEquals("Critical", forecast.flashSurgeIndex.levelStr)
        assertTrue(forecast.euWfdMeasures.any { it.contains("Measure #22") })
    }

    @Test
    fun testAlgaeBloomEscalationForecast() {
        val report = UsgsWaterHealthReport(
            locationName = "Warm Alkaline Lake Channel",
            siteId = "USGS-30000000",
            ph = 8.6, // High alkaline
            turbidityNtu = 6.5,
            dissolvedOxygenMgL = 5.8,
            waterTempCelsius = 26.5, // Warm water
            streamFlowCfs = 120.0,
            gaugeHeightFt = 2.5,
            isDataAvailable = true
        )

        val forecast = HydroClimaticPredictiveEngine.generate48HourForecast(report)

        assertTrue(forecast.trendCategory == HydroTrendCategory.ALGAE_BLOOM_ESCALATION || forecast.trendCategory == HydroTrendCategory.MULTI_HAZARD_ALERT)
        assertTrue(forecast.algaeBloomRiskIndex.score >= 50)
        assertTrue(forecast.euWfdMeasures.any { it.contains("Measure #14") })
    }

    @Test
    fun testHypoxicCrashForecast() {
        val report = UsgsWaterHealthReport(
            locationName = "Stagnant Oxygen Depleted Stream",
            siteId = "USGS-40000000",
            ph = 7.1,
            turbidityNtu = 8.0,
            dissolvedOxygenMgL = 3.5, // Low DO
            waterTempCelsius = 22.0,
            streamFlowCfs = 45.0,
            gaugeHeightFt = 1.2,
            isDataAvailable = true
        )

        val forecast = HydroClimaticPredictiveEngine.generate48HourForecast(report)

        assertTrue(forecast.trendCategory == HydroTrendCategory.HYPOXIC_CRASH_WARNING || forecast.hypoxiaStressIndex.score >= 50)
        assertEquals("Critical", forecast.hypoxiaStressIndex.levelStr)
        assertTrue(forecast.euWfdMeasures.any { it.contains("Measure #08") })
    }

    @Test
    fun testUnavailableDataForecast() {
        val report = UsgsWaterHealthReport(
            locationName = "Offline Site",
            siteId = "UNAVAILABLE",
            isDataAvailable = false
        )

        val forecast = HydroClimaticPredictiveEngine.generate48HourForecast(report)

        assertEquals("Insufficient Data", forecast.forecastTitle)
        assertEquals(0, forecast.overallRiskScore)
    }
}
