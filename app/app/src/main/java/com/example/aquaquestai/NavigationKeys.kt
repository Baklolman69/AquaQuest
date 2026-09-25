package com.example.aquaquestai

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

// Screen navigation keys
@Serializable data object MapTab : NavKey
@Serializable data object CameraTab : NavKey
@Serializable data object QuestsTab : NavKey
@Serializable data object ProfileTab : NavKey
