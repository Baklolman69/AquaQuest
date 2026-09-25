package com.example.aquaquestai.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AquaHealthExposureEngineTest {

    @Test
    fun testLowExposureRelevanceWhenNoExposureOrSymptoms() {
        val result = AquaHealthExposureEngine.evaluateExposure(
            symptoms = emptyList(),
            otherSymptomText = "",
            exposures = listOf("🚫 No known water exposure"),
            timing = "Today",
            waterReport = null
        )

        assertEquals(AquaHealthRelevance.LOW, result.relevanceLevel)
        assertTrue(result.relevanceTitle.contains("Low Environmental Relevance"))
        assertTrue(result.limitationsBanner.contains("environmental exposure context"))
    }

    @Test
    fun testElevatedExposureRelevanceWithDrinkingUntreatedWaterAndGiSymptoms() {
        val sampleWaterReport = UsgsWaterHealthReport(
            siteId = "01646500",
            locationName = "POTOMAC RIVER NEAR WASH, DC",
            observedTimestampStr = "2026-09-22T10:00:00Z",
            streamFlowCfs = 4500.0,
            waterTempCelsius = 26.5,
            ph = 8.6,
            turbidityNtu = 45.0,
            dissolvedOxygenMgL = 4.2,
            gaugeHeightFt = 4.1,
            oneHealthRiskScore = 7,
            explainableSummary = "Elevated turbidity and water temperature"
        )

        val result = AquaHealthExposureEngine.evaluateExposure(
            symptoms = listOf("🚽 Diarrhea", "🤢 Nausea", "🌡️ Fever"),
            otherSymptomText = "",
            exposures = listOf("🚰 Drank untreated surface water"),
            timing = "1–2 days ago",
            waterReport = sampleWaterReport
        )

        assertEquals(AquaHealthRelevance.ELEVATED, result.relevanceLevel)
        assertTrue(result.relevanceTitle.contains("Elevated Environmental Relevance"))
        assertNotNull(result.urgentWarningText)
        assertTrue(result.urgentWarningText!!.contains("medical evaluation"))
        assertTrue(result.healthSources.any { it.name == "CDC" })
        assertTrue(result.healthSources.any { it.name == "WHO" })
    }

    @Test
    fun testNonDiagnosticConstraintsInAIExplanation() {
        val result = AquaHealthExposureEngine.evaluateExposure(
            symptoms = listOf("🧴 Skin irritation", "🔴 Rash"),
            otherSymptomText = "",
            exposures = listOf("🏊 Swam in freshwater"),
            timing = "3–7 days ago",
            waterReport = null
        )

        val text = result.aiInterpretationText
        assertTrue(!text.contains("You have ", ignoreCase = true))
        assertTrue(!text.contains("The water caused", ignoreCase = true))
        assertTrue(result.limitationsBanner.contains("cannot determine the cause"))
    }
}
