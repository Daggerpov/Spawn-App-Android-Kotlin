package com.danielagapov.spawn

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.danielagapov.spawn.core.session.Spawn
import com.mapbox.common.TelemetryUtils

class SpawnApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        // Mapbox sends anonymous location and usage telemetry to Mapbox by default.
        // Spawn doesn't need it, and leaving it off keeps the Play data safety form
        // limited to data Spawn's own backend receives.
        TelemetryUtils.setEventsCollectionState(false) { }
        Spawn.init(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context).crossfade(true).build()
}
