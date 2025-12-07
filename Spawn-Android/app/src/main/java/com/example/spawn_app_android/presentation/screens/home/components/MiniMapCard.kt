package com.example.spawn_app_android.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
@OptIn(ExperimentalMaterial3Api::class)
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
                    modifier = Modifier.size(30.dp),
                    tint = null
                )
            }
            
            // Floating location info bar at bottom
            MiniMapButtons(
                locationName = locationName,
                onClick = onClick
            )
        }
    }
}

@Composable
fun BoxScope.MiniMapButtons(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
    locationName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location name row
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = backgroundSecondaryDark.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .height(36.dp)
                .padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = spawnIndigo
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = locationName,
                color = white,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // View in Maps button
        if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = spawnIndigo
                ),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 0.dp
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_direction_sign),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = white
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View in Maps",
                    color = white,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
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
