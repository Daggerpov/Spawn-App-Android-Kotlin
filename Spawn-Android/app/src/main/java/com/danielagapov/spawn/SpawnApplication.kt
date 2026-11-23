package com.danielagapov.spawn

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.danielagapov.spawn.core.push.Push
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.mapbox.common.TelemetryUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SpawnApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        // Mapbox sends anonymous location and usage telemetry to Mapbox by default.
        // Spawn doesn't need it, and leaving it off keeps the Play data safety form
        // limited to data Spawn's own backend receives.
        TelemetryUtils.setEventsCollectionState(false) { }
        Spawn.init(this)
        Push.createChannel(this)
        watchSessionForPush()
    }

    /** Registers the push token whenever someone is signed in, and drops it after they sign out. */
    private fun watchSessionForPush() {
        MainScope().launch {
            var hadUser = false
            Spawn.auth.collect { state ->
                when (state) {
                    // The backend only accepts device tokens from fully onboarded (ACTIVE) users.
                    is AuthState.SignedIn -> {
                        hadUser = true
                        Push.syncToken()
                    }
                    is AuthState.Onboarding -> hadUser = true
                    AuthState.SignedOut -> if (hadUser) {
                        hadUser = false
                        Push.onSignedOut()
                    }
                    AuthState.Loading -> Unit
                }
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context).crossfade(true).build()
}
