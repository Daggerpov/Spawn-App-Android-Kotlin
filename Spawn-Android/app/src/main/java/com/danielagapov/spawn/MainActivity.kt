package com.danielagapov.spawn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.danielagapov.spawn.ui.components.InAppBannerHost
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.danielagapov.spawn.core.push.Push
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.navigation.AppRoot
import com.danielagapov.spawn.ui.theme.SpawnTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) Push.syncToken()
        }

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
        lifecycleScope.launch {
            Spawn.auth.first { it is AuthState.SignedIn }
            maybeAskForNotifications()
        }
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

    /** Android 13+ needs runtime permission for notifications. Ask once, the first time someone is signed in. */
    private fun maybeAskForNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        val prefs = getSharedPreferences("spawn_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("asked_notifications", false)) return
        prefs.edit().putBoolean("asked_notifications", true).apply()
        askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
