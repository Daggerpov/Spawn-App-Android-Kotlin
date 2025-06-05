package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.navigation.EventMapToggle
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState


@Composable
fun MapPage(
    onNavigateToEventsPage: () -> Unit
) {
    SpawnAppAndroidTheme {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxSize() // ensure map is max size
                    .background(
                        color = Color(0xFFE7E7DD)
                    )

            ) {
                MapContainer()
                Column (
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //verticalArrangement = Arrangement.SpaceBetween
                ) {
                    VerticalSpacer(40)

                    EventMapToggle(true, 40, onNavigateToEventsPage)

                    VerticalSpacer(30)
                    FriendFilterReel(filters = arrayOf("everyone", "close friends", "sports"))
                    VerticalSpacer(570)
                    FriendButton()
                }

            } // Box
        } // Column
    }
}

@Composable
fun MapContainer() {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(2.0)
                    center(Point.fromLngLat(-98.0, 39.5))
                    pitch(0.0)
                    bearing(0.0)
                }
            },
        )
    }

}

@Composable
fun FriendButton() {

    Box(
        modifier = Modifier
            .width(46.dp)
            .height(46.dp)
            .background(
                color = Color(0x00000000),
                shape = RoundedCornerShape(100)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1D3D3D),
                shape = CircleShape
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.friends_icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFF1D3D3D)),
            modifier = Modifier
                .align(Alignment.Center)
                .width(21.dp)
                .height(21.dp)
        )
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
    //MapPage()
}