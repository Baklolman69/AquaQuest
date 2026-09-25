package com.example.aquaquestai.data

import com.example.aquaquestai.data.engine.EvidenceEngine
import com.example.aquaquestai.data.engine.LocalBaselineEngine
import com.example.aquaquestai.domain.model.AquaEventStatus
import com.example.aquaquestai.domain.model.BaselineWindow
import com.example.aquaquestai.domain.model.CitizenObservationCategory
import com.example.aquaquestai.domain.model.Evidence
import com.example.aquaquestai.domain.model.EvidenceSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceEngineTest {

    @Test
    fun testLocalBaselineDeviationCalculation() {
        val historical = listOf(7.2, 7.5, 7.8, 8.0, 7.6) // avg = 7.62
        val baseline = LocalBaselineEngine.evaluateParameterBaseline(
            parameter = "Turbidity",
            currentValue = 12.7,
            historicalValues = historical,
            unit = "NTU",
            window = BaselineWindow.DAYS_7
        )

        assertNotNull(baseline.percentageDeviation)
        assertTrue(baseline.percentageDeviation!! > 50.0)
        assertTrue(baseline.statusText.contains("above recent local baseline"))
    }

    @Test
    fun testInsufficientHistoricalDataState() {
        val baseline = LocalBaselineEngine.evaluateParameterBaseline(
            parameter = "Turbidity",
            currentValue = 12.7,
            historicalValues = emptyList(),
            unit = "NTU"
        )

        assertEquals("Insufficient historical data for baseline", baseline.statusText)
    }

    @Test
    fun testAquaEventCreationAndStatusTransition() {
        val ev1 = Evidence(
            id = "e1",
            sourceType = EvidenceSourceType.USGS_ENVIRONMENTAL,
            parameter = "Turbidity",
            value = "12.7",
            unit = "NTU",
            observationTimestamp = System.currentTimeMillis()
        )
        val ev2 = Evidence(
            id = "e2",
            sourceType = EvidenceSourceType.CITIZEN_OBSERVATION,
            parameter = "Unusual Water Color",
            value = "Brownish",
            unit = "",
            observationTimestamp = System.currentTimeMillis(),
            category = CitizenObservationCategory.UNUSUAL_WATER_COLOR,
            observerId = "usr-1"
        )
        val ev3 = Evidence(
            id = "e3",
            sourceType = EvidenceSourceType.CITIZEN_OBSERVATION,
            parameter = "Foam",
            value = "Observed",
            unit = "",
            observationTimestamp = System.currentTimeMillis(),
            category = CitizenObservationCategory.FOAM,
            observerId = "usr-2"
        )
        val ev4 = Evidence(
            id = "e4",
            sourceType = EvidenceSourceType.CITIZEN_OBSERVATION,
            parameter = "Waste",
            value = "Observed",
            unit = "",
            observationTimestamp = System.currentTimeMillis(),
            category = CitizenObservationCategory.WASTE_DEBRIS,
            observerId = "usr-3"
        )

        val event = EvidenceEngine.createAquaEvent(
            id = "event-test-01",
            title = "Test Stream Anomaly",
            locationName = "Test River",
            latitude = 30.0,
            longitude = -97.0,
            evidenceItems = listOf(ev1, ev2, ev3, ev4)
        )

        assertEquals(AquaEventStatus.CORROBORATED, event.status)
        assertEquals(3, event.verificationSummary.independentObserverCount)
        assertTrue(event.waterStoryTimeline.isNotEmpty())
    }
}
