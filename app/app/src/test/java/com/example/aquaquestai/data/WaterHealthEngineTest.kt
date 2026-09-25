package com.example.aquaquestai.data

import org.junit.Assert.*
import org.junit.Test

class WaterHealthEngineTest {

    @Test
    fun testNormalObservationsEvaluation() {
        val report = UsgsWaterHealthReport(
            locationName = "Normal River",
            siteId = "USGS-12345678",
            ph = 7.4,
            turbidityNtu = 2.4,
            dissolvedOxygenMgL = 8.5,
            waterTempCelsius = 18.0,
            streamFlowCfs = 240.0,
            isDataAvailable = true
        )

        val assessment = WaterHealthEngine.evaluateReport(report)
        assertEquals("Normal Indicator Range", assessment.overallStatus)
        assertEquals(1, assessment.riskSignalScore)
        assertEquals("High", assessment.confidenceLevel)
        assertTrue(assessment.detectedSignals.isEmpty())
        assertEquals(5, assessment.evidenceList.size)
        assertTrue(assessment.riskSignalExplanation.contains("No significant anomaly detected"))
    }

    @Test
    fun testHighTurbiditySignalEvaluation() {
        val report = UsgsWaterHealthReport(
            locationName = "High Turbidity Stream",
            siteId = "USGS-87654321",
            ph = 7.2,
            turbidityNtu = 14.5, // > 10.0 NTU
            dissolvedOxygenMgL = 8.0,
            waterTempCelsius = 19.0,
            streamFlowCfs = 150.0,
            isDataAvailable = true
        )

        val assessment = WaterHealthEngine.evaluateReport(report)
        assertTrue(assessment.riskSignalScore >= 4)
        assertEquals(1, assessment.detectedSignals.size)
        assertEquals("Turbidity", assessment.detectedSignals[0].indicatorName)
        assertEquals(SignalSeverity.HIGH, assessment.detectedSignals[0].signalSeverity)
    }

    @Test
    fun testPhOutsideConfiguredReferenceRange() {
        val report = UsgsWaterHealthReport(
            locationName = "Acidic Stream",
            siteId = "USGS-99999999",
            ph = 5.8, // Outside 6.5 - 8.5
            turbidityNtu = 3.0,
            dissolvedOxygenMgL = 8.2,
            waterTempCelsius = 16.0,
            isDataAvailable = true
        )

        val assessment = WaterHealthEngine.evaluateReport(report)
        assertTrue(assessment.riskSignalScore >= 4)
        val phSignal = assessment.detectedSignals.firstOrNull { it.indicatorName == "pH Balance" }
        assertNotNull(phSignal)
        assertEquals(SignalSeverity.HIGH, phSignal!!.signalSeverity)
    }

    @Test
    fun testMissingIndicatorsConfidenceHandling() {
        val report = UsgsWaterHealthReport(
            locationName = "Partial Gauge",
            siteId = "USGS-11112222",
            ph = null,
            turbidityNtu = null,
            dissolvedOxygenMgL = null,
            waterTempCelsius = 15.0, // Only temp present
            streamFlowCfs = null,
            isDataAvailable = true
        )

        val assessment = WaterHealthEngine.evaluateReport(report)
        assertEquals("Low", assessment.confidenceLevel)
        assertEquals(1, assessment.evidenceList.size)
    }

    @Test
    fun testUnavailableDataEvaluation() {
        val report = UsgsWaterHealthReport(
            locationName = "Offline Gauge",
            siteId = "UNAVAILABLE",
            isDataAvailable = false
        )

        val assessment = WaterHealthEngine.evaluateReport(report)
        assertEquals("Insufficient Data", assessment.overallStatus)
        assertEquals(1, assessment.riskSignalScore)
        assertEquals("Insufficient Data", assessment.confidenceLevel)
        assertTrue(assessment.evidenceList.isEmpty())
    }
}
