package com.example.spawn_app_android.presentation.screens.Utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetDarkStatusBarIcons() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return

    SideEffect {
        // Make status bar icons dark (suitable for light background)
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }
}