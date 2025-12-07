package com.example.spawn_app_android.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.example.spawn_app_android.presentation.theme.backgroundSecondaryDark
import com.example.spawn_app_android.presentation.theme.spawnIndigo
import com.example.spawn_app_android.presentation.theme.white
import com.example.spawn_app_android.utils.rememberMiniMapViewportState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap

/**
 * A card component that displays a mini map view showing the event location.
 * The map is non-interactive and displays as a preview of the event location.
 *
 * @param modifier Modifier for the card
 * @param locationName The name of the location to display
 * @param latitude The latitude coordinate of the event location
 * @param longitude The longitude coordinate of the event location
 * @param onClick Optional click handler for when the card is tapped
 */
@Composable
fun MiniMapCard(
    modifier: Modifier = Modifier,
    locationName: String,
    latitude: Double,
    longitude: Double,
    onClick: (() -> Unit)? = null
) {
    val mapViewportState = rememberMiniMapViewportState(
        longitude = longitude,
        latitude = latitude
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundSecondaryDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        // Map container with rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = {},
                logo = {},
                attribution = {},
                compass = {}
            )
            
            // Location pin overlay at center
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location_pin),
                    contentDescription = "Event location",
                    modifier = Modifier.size(32.dp),
                    tint = spawnIndigo
                )
            }
            
            // Floating location info bar at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = backgroundSecondaryDark.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = spawnIndigo
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = locationName,
                        color = white,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Directions hint icon
                    if (onClick != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = "View directions",
                            modifier = Modifier.size(18.dp),
                            tint = white.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Overload that accepts a Point instead of separate lat/lng values.
 */
@Composable
fun MiniMapCard(
    modifier: Modifier = Modifier,
    locationName: String,
    point: Point,
    onClick: (() -> Unit)? = null
) {
    MiniMapCard(
        modifier = modifier,
        locationName = locationName,
        latitude = point.latitude(),
        longitude = point.longitude(),
        onClick = onClick
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1F1E1E)
@Composable
fun MiniMapCardPreview() {
    // Preview with San Francisco coordinates
    MiniMapCard(
        locationName = "Golden Gate Park, San Francisco",
        latitude = 37.7694,
        longitude = -122.4862,
        onClick = {}
    )
}
