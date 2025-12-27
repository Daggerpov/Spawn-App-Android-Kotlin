package com.example.spawn_app_android.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.res.painterResource
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.example.spawn_app_android.presentation.screens.Utils.SetDarkStatusBarIcons
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.CameraOptions


@Composable
fun MapPage() {
    SetDarkStatusBarIcons()

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val chips = arrayOf("Late Night", "Evening", "Afternoon",
        "Next Hour", "Happening Now", "All Activities")

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(13.0)
            center(Point.fromLngLat(-123.2460, 49.2606)) // UBC Vancouver
            pitch(0.0)
            bearing(0.0)
        }
    }

    var userLocation by remember { mutableStateOf<Point?>(null) }

    SpawnAppAndroidTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White
                )
        ) {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = { },
                compass = { },
            ) {
                if (hasLocationPermission) {
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                            puckBearingEnabled = true
                            puckBearing = PuckBearing.HEADING
                            locationPuck = createDefault2DPuck(withBearing = true)
                        }
                        mapView.location.addOnIndicatorPositionChangedListener { point ->
                            userLocation = point
                        }
                    }
                }
            }

            Column (
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Center on location button
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(48.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            userLocation?.let { location ->
                                mapViewportState.setCameraOptions(
                                    CameraOptions.Builder()
                                        .center(location)
                                        .zoom(12.0)
                                        .build()
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_compass),
                        contentDescription = "Center on my location",
                        tint = Color(0xFF1D3D3D),
                        modifier = Modifier.size(24.dp)
                    )
                }

                ChipGroup(chips, 5, false)
            }
        }
    }
}

@Composable
fun ChipGroup(chips: Array<String>, selected: Int, expanded: Boolean) {

    var isClicked by remember { mutableStateOf(expanded) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {isClicked = !isClicked},
            )
    ) {
        if (!isClicked) {
            Chip(chips[selected], true)
        } else {
            Column {
                for (chip in chips) {
                    if (chip == chips[selected]) {
                        Chip(chip, true)
                    } else {
                        Chip(chip, false)
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .padding(12.dp, 0.dp, 12.dp, 12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(255, 255, 255),
                    shape = RoundedCornerShape(44.dp)
                )
                .height(44.dp)
                .width(140.dp),
            contentAlignment = Alignment.Center

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(48, 217, 150),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .height(10.dp)
                            .width(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun VerticalSpacer(height: Int) {
    Box(modifier = Modifier.height(height.dp))
}

// preview
@Preview(showBackground = true,
    showSystemUi = true,
    name = "Map")
@Composable
fun Preview() {
    MapPage()
}