package com.example.spawn_app_android.presentation.screens.activities.flow

import com.mapbox.geojson.Point

data class LocationSuggestion(
    val name: String,
    val address: String,
    val point: Point? = null,
    val isCurrentLocation: Boolean = false
)