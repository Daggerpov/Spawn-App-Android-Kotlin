package com.danielagapov.spawn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.danielagapov.spawn.ui.components.InAppBannerHost
import android.content.Intent
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
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Spawn.handleLink(intent.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { Spawn.auth.value == AuthState.Loading }
        if (Spawn.auth.value == AuthState.Loading) {
            lifecycleScope.launch { Spawn.restoreSession() }
        }
        if (savedInstanceState == null) Spawn.handleLink(intent?.data)
        enableEdgeToEdge()
        setContent {
            SpawnTheme {
                Box(Modifier.fillMaxSize()) {
                    AppRoot()
                    InAppBannerHost(Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
