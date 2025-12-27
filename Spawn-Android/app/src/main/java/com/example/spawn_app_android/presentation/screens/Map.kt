package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.example.spawn_app_android.presentation.screens.Utils.SetDarkStatusBarIcons
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState


@Composable
fun MapPage() {
    SetDarkStatusBarIcons()

    val chips = arrayOf("Late Night", "Evening", "Afternoon",
        "Next Hour", "Happening Now", "All Activities")

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
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(13.0)
                        center(Point.fromLngLat(-123.2460, 49.2606)) // UBC Vancouver
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
                scaleBar = { },
            )

            Column (
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
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