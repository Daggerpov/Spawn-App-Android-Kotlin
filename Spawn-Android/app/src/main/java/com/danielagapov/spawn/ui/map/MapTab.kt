package com.danielagapov.spawn.ui.map

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.ActivityStatus
import com.danielagapov.spawn.core.util.Dates
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.ui.theme.ActivityPalette
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Time filters from the Figma "Open Filters" frame, same rules as iOS MapView.updateFilteredActivities. */
enum class TimeFilter(val label: String) {
    LateNight("Late Night"), Evening("Evening"), Afternoon("Afternoon"), NextHour("In the next hour"), Now("Happening Now"), All("All Activities");

    fun matches(a: FullFeedActivityDTO, now: Instant = Instant.now()): Boolean {
        val start = Dates.parse(a.startTime) ?: return false
        val zone = ZoneId.systemDefault()
        val local = start.atZone(zone)
        val today = local.toLocalDate() == LocalDate.now(zone)
        return when (this) {
            All -> true
            Now -> ActivityStatus.of(a).isLive
            NextHour -> start.isAfter(now) && !start.isAfter(now.plusSeconds(3600))
            Afternoon -> today && local.hour in 12..16
            Evening -> today && local.hour in 17..20
            LateNight -> today && (local.hour >= 21 || local.hour < 4)
        }
    }
}

private val UBC = Point.fromLngLat(-123.2460, 49.2606)

/** Map tab (Figma "Map View"): activity pins with their emoji, 3D / locate controls, compass and time filter. */
@Composable
fun MapTab(activities: List<FullFeedActivityDTO>, onOpenActivity: (FullFeedActivityDTO) -> Unit, bottomPadding: Dp) {
    val context = LocalContext.current
    val me by UserLocation.location.collectAsState()
    var filter by rememberSaveable { mutableStateOf(TimeFilter.All) }
    var filtersOpen by remember { mutableStateOf(false) }
    var threeD by rememberSaveable { mutableStateOf(false) }
    val shown = remember(activities, filter) { activities.filter { it.location != null && filter.matches(it) } }

    val viewport = rememberMapViewportState {
        val start = me?.let { Point.fromLngLat(it.longitude, it.latitude) }
            ?: activities.firstOrNull { it.location != null }?.location?.let { Point.fromLngLat(it.longitude, it.latitude) }
            ?: UBC
        setCameraOptions { center(start); zoom(14.0); pitch(0.0) }
    }
    LaunchedEffect(Unit) { UserLocation.refresh(context) }

    BackHandler(filtersOpen) { filtersOpen = false }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = viewport,
            scaleBar = {},
            compass = { Compass(alignment = Alignment.TopEnd, contentPadding = PaddingValues(top = 200.dp, end = 20.dp)) },
            // Keep the Mapbox logo + attribution visible above the floating tab bar
            logo = { Logo(contentPadding = PaddingValues(start = 8.dp, bottom = bottomPadding + 4.dp)) },
            attribution = { Attribution(contentPadding = PaddingValues(start = 96.dp, bottom = bottomPadding + 4.dp)) },
        ) {
            MapEffect(Unit) { view ->
                view.location.updateSettings {
                    enabled = true
                    locationPuck = createDefault2DPuck(withBearing = false)
                }
            }
            shown.forEach { a ->
                val loc = a.location ?: return@forEach
                key(a.id) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(Point.fromLngLat(loc.longitude, loc.latitude))
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                            allowOverlap(true)
                        },
                    ) {
                        ActivityPin(a.icon ?: "⭐️", ActivityPalette.color(Spawn.colors.indexFor(a.id)), live = ActivityStatus.of(a).isLive) { onOpenActivity(a) }
                    }
                }
            }
        }

        // 3D + locate (Figma's white control stack, top right)
        Column(
            Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
                .width(48.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
        ) {
            Box(
                Modifier.size(48.dp).clickable {
                    threeD = !threeD
                    viewport.easeTo(
                        com.mapbox.maps.CameraOptions.Builder().pitch(if (threeD) 60.0 else 0.0).zoom(if (threeD) 16.0 else 14.0).build(),
                        MapAnimationOptions.mapAnimationOptions { duration(600) },
                    )
                },
                contentAlignment = Alignment.Center,
            ) { Text("3D", style = onest(17.sp, if (threeD) FontWeight.Bold else FontWeight.Medium), color = if (threeD) Palette.Indigo600 else Palette.Gray800) }
            HorizontalDivider(color = Palette.Gray100)
            Box(
                Modifier.size(48.dp).clickable {
                    UserLocation.refresh(context)
                    me?.let {
                        viewport.flyTo(
                            com.mapbox.maps.CameraOptions.Builder().center(Point.fromLngLat(it.longitude, it.latitude)).zoom(15.0).build(),
                            MapAnimationOptions.mapAnimationOptions { duration(800) },
                        )
                    }
                },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.NearMe, "My location", tint = Palette.Gray800, modifier = Modifier.size(22.dp)) }
        }

        if (shown.isEmpty()) {
            Text(
                if (filter == TimeFilter.All) "No activities on the map yet" else "Nothing ${filter.label.lowercase()}",
                style = onest(14.sp, FontWeight.Medium), color = Palette.Gray800,
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 20.dp)
                    .shadow(4.dp, RoundedCornerShape(100.dp)).clip(RoundedCornerShape(100.dp)).background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }

        // Dimmed overlay + filter list (Figma "Open Filters")
        AnimatedVisibility(filtersOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.55f))
                    .clickable(remember { MutableInteractionSource() }, null) { filtersOpen = false },
            )
        }
        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = bottomPadding + 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            if (filtersOpen) {
                TimeFilter.entries.forEach { f ->
                    FilterPill(f.label, selected = f == filter, wide = true) { filter = f; filtersOpen = false }
                }
            } else {
                FilterPill(filter.label, selected = true, wide = false) { filtersOpen = true }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, wide: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .then(if (wide) Modifier.width(166.dp) else Modifier)
            .shadow(6.dp, RoundedCornerShape(100.dp))
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Palette.HappeningNow))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = onest(16.sp, FontWeight.Medium), color = Palette.Gray900)
    }
}

/** The Figma map marker: a colored teardrop with the activity's emoji. */
@Composable
private fun ActivityPin(emoji: String, color: Color, live: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(44.dp, 58.dp).clickable(remember { MutableInteractionSource() }, null, onClick = onClick)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val r = w / 2f
            val path = Path().apply {
                moveTo(w / 2f, h)
                cubicTo(w * 0.36f, h * 0.80f, 0f, h * 0.60f, 0f, r)
                arcTo(androidx.compose.ui.geometry.Rect(0f, 0f, w, w), 180f, 180f, false)
                cubicTo(w, h * 0.60f, w * 0.64f, h * 0.80f, w / 2f, h)
                close()
            }
            drawPath(path, Color.Black.copy(alpha = 0.18f), alpha = 1f)
            translate(0f, -2f) { drawPath(path, color) }
            drawCircle(Color.White.copy(alpha = 0.18f), radius = r * 0.78f, center = Offset(r, r - 2f))
        }
        Text(emoji, fontSize = 20.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        if (live) {
            Box(Modifier.align(Alignment.TopEnd).size(12.dp).clip(CircleShape).background(Color.White).padding(2.dp).clip(CircleShape).background(Palette.HappeningNow))
        }
    }
}
