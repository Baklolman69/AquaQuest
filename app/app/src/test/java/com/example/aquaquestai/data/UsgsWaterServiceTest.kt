package com.example.aquaquestai.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class UsgsWaterServiceTest {

    @Test
    fun testCelsiusToFahrenheitConversion() {
        val temp0C = 0.0
        val f0 = ((temp0C * 9.0 / 5.0) + 32.0).let { (it * 10).roundToInt() / 10.0 }
        assertEquals(32.0, f0, 0.01)

        val temp100C = 100.0
        val f100 = ((temp100C * 9.0 / 5.0) + 32.0).let { (it * 10).roundToInt() / 10.0 }
        assertEquals(212.0, f100, 0.01)

        val temp25C = 25.0
        val f25 = ((temp25C * 9.0 / 5.0) + 32.0).let { (it * 10).roundToInt() / 10.0 }
        assertEquals(77.0, f25, 0.01)
    }

    @Test
    fun testParseValidMeasurement() {
        // Valid readings
        assertEquals(7.4, UsgsWaterService.parseValidMeasurement("7.4")!!, 0.01)
        assertEquals(12.8, UsgsWaterService.parseValidMeasurement(" 12.8 ")!!, 0.01)

        // USGS missing/invalid value flags
        assertNull(UsgsWaterService.parseValidMeasurement("-999999.0"))
        assertNull(UsgsWaterService.parseValidMeasurement("-99999.0"))
        assertNull(UsgsWaterService.parseValidMeasurement("-9999.0"))
        assertNull(UsgsWaterService.parseValidMeasurement("9999.0"))
        assertNull(UsgsWaterService.parseValidMeasurement(""))
        assertNull(UsgsWaterService.parseValidMeasurement(null))
        assertNull(UsgsWaterService.parseValidMeasurement("NaN"))
    }

    @Test
    fun testWeissDoSaturatedPercent() {
        val sat = UsgsWaterService.computeWeissDoSaturatedPercent(8.4, 16.5)
        assertNotNull(sat)
        assertTrue(sat!! in 70.0..110.0)

        // Invalid inputs should return null
        assertNull(UsgsWaterService.computeWeissDoSaturatedPercent(-1.0, 15.0))
        assertNull(UsgsWaterService.computeWeissDoSaturatedPercent(8.0, 60.0))
    }

    @Test
    fun testFetchEmptyQueryReturnsUnavailableStateWithoutFakeData() = runBlocking {
        val report = UsgsWaterService.fetchWaterDataByLocation("")
        assertFalse(report.isDataAvailable)
        assertEquals("UNAVAILABLE", report.siteId)
        assertNull(report.streamFlowCfs)
        assertNull(report.waterTempCelsius)
        assertNull(report.ph)
        assertNull(report.turbidityNtu)
        assertNull(report.dissolvedOxygenMgL)
    }
}
