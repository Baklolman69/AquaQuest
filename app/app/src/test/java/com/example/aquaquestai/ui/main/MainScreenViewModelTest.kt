package com.example.aquaquestai.ui.main

import com.example.aquaquestai.presentation.AquaQuestViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun testAquaQuestViewModelInitialization() {
        val viewModel = AquaQuestViewModel()
        assertNotNull(viewModel.mapMarkers)
        assertTrue(viewModel.mapMarkers.isNotEmpty())
    }

    @Test
    fun testAddObservationIncrementsMarkerCount() {
        val viewModel = AquaQuestViewModel()
        val initialSize = viewModel.mapMarkers.size
        viewModel.addObservation(
            title = "Test River Station",
            snippet = "Flow: 10 cfs",
            type = com.example.aquaquestai.presentation.screens.map.MarkerType.HEALTHY,
            ph = 7.2,
            turbidity = "1.2 NTU",
            microplasticRisk = "Low"
        )
        assertEquals(initialSize + 1, viewModel.mapMarkers.size)
    }
}
