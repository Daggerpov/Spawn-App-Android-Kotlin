package com.danielagapov.spawn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.navigation.AppRoot
import com.danielagapov.spawn.ui.theme.SpawnTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { Spawn.auth.value == AuthState.Loading }
        if (Spawn.auth.value == AuthState.Loading) {
            lifecycleScope.launch { Spawn.restoreSession() }
        }
        enableEdgeToEdge()
        setContent {
            SpawnTheme {
                AppRoot()
            }
        }
    }
}
