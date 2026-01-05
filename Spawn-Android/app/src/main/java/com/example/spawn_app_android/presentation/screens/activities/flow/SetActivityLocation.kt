package com.example.spawn_app_android.presentation.screens.activities.flow

import android.location.Geocoder
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.Utils.SetDarkStatusBarIcons
import com.example.spawn_app_android.presentation.screens.Utils.getNotifBarPadding
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel
import com.example.spawn_app_android.presentation.screens.activities.CreateActivityEvent
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityLocationConstants.DEFAULT_PEEK_HEIGHT
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.geojson.BoundingBox
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetActivityLocation(
    onNext: () -> Unit,
    activityViewModel: ActivityViewModel,
    onBack: () -> Unit
) {
    SetDarkStatusBarIcons()

    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

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

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(13.0)
            center(Point.fromLngLat(-123.2460, 49.2606)) // Default center
            pitch(0.0)
            bearing(0.0)
        }
    }

    var userLocation by remember { mutableStateOf<Point?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("Fetching location...") }
    var nearbyPlaces by remember { mutableStateOf<List<LocationSuggestion>>(emptyList()) }

    val discover = remember { Discover.create() }

    LaunchedEffect(userLocation) {
        userLocation?.let { point ->
            withContext(Dispatchers.IO) {
                // Reverse geocode for current address
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(point.latitude(), point.longitude(), 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        currentAddress = address.getAddressLine(0) ?: "Unknown location"
                    }
                } catch (e: Exception) {
                    currentAddress = "Unable to fetch address"
                    Log.d("SetActivityLocation", "Failed to fetch address: ${e.localizedMessage}")
                }

                // Fetch nearby popular places using Mapbox Discover
                try {
                    // Create a ~10km bounding box around the user's location
                    val latOffset = 0.045 // ~5km in latitude
                    val lngOffset = 0.055 // ~5km in longitude (varies by latitude)
                    val region = BoundingBox.fromPoints(
                        Point.fromLngLat(point.longitude() - lngOffset, point.latitude() - latOffset),
                        Point.fromLngLat(point.longitude() + lngOffset, point.latitude() + latOffset)
                    )
                    val response = discover.search(
                        query = DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                        region = region
                    )
                    response.onValue { results ->
                        nearbyPlaces = results.take(5).map { result ->
                            LocationSuggestion(
                                name = result.name,
                                address = result.address.formattedAddress ?: "Unknown address",
                                point = result.coordinate,
                                isCurrentLocation = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Handle error silently, keep empty list
                    Log.d("SetActivityLocation", "Failed to fetch nearby places: ${e.localizedMessage}")
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = colorResource(R.color.white),
        sheetContent = {
            HeaderBtmSheet(onBack)
            Spacer(Modifier.height(23.dp))
            SearchSection(
                activityViewModel = activityViewModel,
                userLocation = userLocation,
                currentAddress = currentAddress,
                nearbyPlaces = nearbyPlaces,
                onLocationSelected = { suggestion ->
                    onNext()
                }
            )
        },
        sheetPeekHeight = DEFAULT_PEEK_HEIGHT
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
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
                            if (!hasCenteredOnUser) {
                                mapViewportState.setCameraOptions(
                                    CameraOptions.Builder()
                                        .center(point)
                                        .zoom(14.0)
                                        .build()
                                )
                                hasCenteredOnUser = true
                            }
                        }
                    }
                }
            }

            HeaderMain(onBack = onBack)
        }
    }
}

@Composable
private fun SearchSection(
    activityViewModel: ActivityViewModel,
    userLocation: Point?,
    currentAddress: String,
    nearbyPlaces: List<LocationSuggestion>,
    onLocationSelected: (LocationSuggestion) -> Unit
) {
    SearchBar(activityViewModel)
    Spacer(Modifier.height(16.dp))
    SearchSuggestions(
        userLocation = userLocation,
        currentAddress = currentAddress,
        nearbyPlaces = nearbyPlaces,
        onLocationSelected = onLocationSelected
    )
}

@Composable
private fun SearchSuggestions(
    userLocation: Point?,
    currentAddress: String,
    nearbyPlaces: List<LocationSuggestion>,
    onLocationSelected: (LocationSuggestion) -> Unit
) {
    val currentLocationSuggestion = LocationSuggestion(
        name = "Current Location",
        address = currentAddress,
        point = userLocation,
        isCurrentLocation = true
    )

    val allSuggestions = listOf(currentLocationSuggestion) + nearbyPlaces

    LazyColumn(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        items(allSuggestions) { suggestion ->
            LocationSuggestionItem(
                suggestion = suggestion,
                onClick = { onLocationSelected(suggestion) }
            )
        }
    }
}

@Composable
private fun LocationSuggestionItem(
    suggestion: LocationSuggestion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val compassIcon = painterResource(R.drawable.ic_compass)
        val locationIcon = painterResource(R.drawable.ic_location)
        Icon(
            painter = if (suggestion.isCurrentLocation) compassIcon else locationIcon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = suggestion.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = suggestion.address,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SearchBar(activityViewModel: ActivityViewModel) {
    var query by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = SolidColor(Color.Black),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        BasicTextField(
            value = query,
            onValueChange = {
                query = it
                activityViewModel.onEvent(CreateActivityEvent.LocationChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.Black),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.where_at),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun HeaderBtmSheet(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
    ) {
        BackButton(onClick = onBack, R.drawable.ic_back_black)

        Text(
            stringResource(R.string.choose_location),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun HeaderMain(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = getNotifBarPadding() + 35.dp, start = 26.dp, end = 26.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(color = Color.White, shape = CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            BackButton(
                onClick = { onBack() },
                icon = R.drawable.ic_back_black
            )
        }
    }
}

object SetActivityLocationConstants {
    val DEFAULT_PEEK_HEIGHT = 300.dp
}