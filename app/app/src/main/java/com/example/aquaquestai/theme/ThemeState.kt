package com.example.aquaquestai.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeState {
    // Light mode is enabled by default as requested by user
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun setDarkTheme(dark: Boolean) {
        isDarkMode = dark
    }
}
