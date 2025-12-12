package com.example.spawn_app_android.utils

import androidx.compose.runtime.Composable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

/**
 * MapManager.kt
 *
 * Created by Ethan Dsouza on 2025-12-07
 *
 * MapManager provides utility functions and configuration for Mapbox maps.
 * Use this class to create consistent map configurations across the app.
 */
object MapManager {
    
    // Default map settings
    const val DEFAULT_ZOOM = 14.0
    const val MINI_MAP_ZOOM = 15.0
    const val DEFAULT_PITCH = 0.0
    const val DEFAULT_BEARING = 0.0
    
    // Default center (USA center)
    val DEFAULT_CENTER: Point = Point.fromLngLat(-98.0, 39.5)
    
    /**
     * Creates camera options for a specific location with customizable zoom level.
     * 
     * @param longitude The longitude of the location
     * @param latitude The latitude of the location
     * @param zoom The zoom level (default is DEFAULT_ZOOM)
     * @param pitch The camera pitch (default is DEFAULT_PITCH)
     * @param bearing The camera bearing (default is DEFAULT_BEARING)
     * @return CameraOptions configured for the specified location
     */
    fun createCameraOptions(
        longitude: Double,
        latitude: Double,
        zoom: Double = DEFAULT_ZOOM,
        pitch: Double = DEFAULT_PITCH,
        bearing: Double = DEFAULT_BEARING
    ): CameraOptions {
        return CameraOptions.Builder()
            .center(Point.fromLngLat(longitude, latitude))
            .zoom(zoom)
            .pitch(pitch)
            .bearing(bearing)
            .build()
    }
    
    /**
     * Creates camera options for a Point with customizable zoom level.
     * 
     * @param point The Point location
     * @param zoom The zoom level (default is DEFAULT_ZOOM)
     * @param pitch The camera pitch (default is DEFAULT_PITCH)
     * @param bearing The camera bearing (default is DEFAULT_BEARING)
     * @return CameraOptions configured for the specified location
     */
    fun createCameraOptions(
        point: Point,
        zoom: Double = DEFAULT_ZOOM,
        pitch: Double = DEFAULT_PITCH,
        bearing: Double = DEFAULT_BEARING
    ): CameraOptions {
        return CameraOptions.Builder()
            .center(point)
            .zoom(zoom)
            .pitch(pitch)
            .bearing(bearing)
            .build()
    }
    
    /**
     * Creates camera options optimized for mini map display.
     * Uses a higher zoom level for better detail in small views.
     * 
     * @param longitude The longitude of the location
     * @param latitude The latitude of the location
     * @return CameraOptions optimized for mini map display
     */
    fun createMiniMapCameraOptions(
        longitude: Double,
        latitude: Double
    ): CameraOptions {
        return createCameraOptions(
            longitude = longitude,
            latitude = latitude,
            zoom = MINI_MAP_ZOOM,
            pitch = DEFAULT_PITCH,
            bearing = DEFAULT_BEARING
        )
    }
    
    /**
     * Creates camera options optimized for mini map display from a Point.
     * 
     * @param point The Point location
     * @return CameraOptions optimized for mini map display
     */
    fun createMiniMapCameraOptions(point: Point): CameraOptions {
        return createCameraOptions(
            point = point,
            zoom = MINI_MAP_ZOOM,
            pitch = DEFAULT_PITCH,
            bearing = DEFAULT_BEARING
        )
    }
}

/**
 * Composable helper to create and remember a MapViewportState for a specific location.
 * 
 * @param longitude The longitude of the location
 * @param latitude The latitude of the location
 * @param zoom The zoom level
 * @return A remembered MapViewportState configured for the location
 */
@Composable
fun rememberMiniMapViewportState(
    longitude: Double,
    latitude: Double,
    zoom: Double = MapManager.MINI_MAP_ZOOM
): MapViewportState {
    return rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(zoom)
            pitch(MapManager.DEFAULT_PITCH)
            bearing(MapManager.DEFAULT_BEARING)
        }
    }
}

/**
 * Composable helper to create and remember a MapViewportState from a Point.
 * 
 * @param point The Point location
 * @param zoom The zoom level
 * @return A remembered MapViewportState configured for the location
 */
@Composable
fun rememberMiniMapViewportState(
    point: Point,
    zoom: Double = MapManager.MINI_MAP_ZOOM
): MapViewportState {
    return rememberMiniMapViewportState(
        longitude = point.longitude(),
        latitude = point.latitude(),
        zoom = zoom
    )
}
