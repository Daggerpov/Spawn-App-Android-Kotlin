package com.danielagapov.spawn.ui.theme

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

internal enum class Appearance(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark");

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }
}

internal class AppearanceSettings(private val preferences: SharedPreferences) {
    var selected by mutableStateOf(
        Appearance.entries.firstOrNull { it.name == preferences.getString("appearance", null) }
            ?: Appearance.System,
    )
        private set

    fun select(appearance: Appearance) {
        selected = appearance
        preferences.edit().putString("appearance", appearance.name).apply()
    }
}

internal val LocalAppearanceSettings = staticCompositionLocalOf<AppearanceSettings> {
    error("Appearance settings must be provided by the activity")
}
