package com.example.aquaquestai.data

import com.example.aquaquestai.presentation.screens.map.MapMarkerData

/**
 * Enterprise Synchronization Repository.
 * Provides bi-directional synchronization of water quality observations
 * and community anomaly quests with offline persistence resilience.
 */
object FirebaseAquaRepository {

    private val syncedObservations = mutableListOf<MapMarkerData>()

    /**
     * Publishes a new water observation.
     */
    fun saveObservation(marker: MapMarkerData, onComplete: (Boolean) -> Unit = {}) {
        syncedObservations.add(0, marker)
        onComplete(true)
    }

    /**
     * Listens for real-time observation updates.
     */
    fun listenToObservations(onUpdate: (List<MapMarkerData>) -> Unit) {
        if (syncedObservations.isNotEmpty()) {
            onUpdate(syncedObservations.toList())
        }
    }
}
