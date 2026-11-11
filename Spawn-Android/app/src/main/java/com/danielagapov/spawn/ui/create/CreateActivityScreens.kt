package com.danielagapov.spawn.ui.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.core.model.ActivityTypeDTO
import com.danielagapov.spawn.core.util.Dates
import com.danielagapov.spawn.core.util.Distance
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.ui.activity.shareActivity
import com.danielagapov.spawn.ui.components.*
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The Activities tab: type → time → location → confirm → success (Figma "Create Activity Flow"). */
@Composable
fun CreateActivityFlow(vm: CreateActivityViewModel, onDone: () -> Unit, bottomPadding: Dp) {
    LaunchedEffect(Unit) { if (vm.types.isEmpty()) vm.load() }
    BackHandler(enabled = vm.step != CreateStep.Type && vm.step != CreateStep.Success) { vm.step = CreateStep.entries[vm.step.ordinal - 1] }
    Box(Modifier.fillMaxSize().background(SpawnTheme.colors.background)) {
        when (vm.step) {
            CreateStep.Type -> TypeStep(vm, bottomPadding)
            CreateStep.Time -> TimeStep(vm, bottomPadding)
            CreateStep.Location -> LocationStep(vm, bottomPadding)
            CreateStep.Confirm -> ConfirmStep(vm, bottomPadding)
            CreateStep.Success -> SuccessStep(vm, bottomPadding, onDone = { vm.reset(); onDone() })
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int = 3) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        repeat(total) { i ->
            Box(Modifier.size(width = 32.dp, height = 6.dp).clip(CircleShape).background(if (i <= current) Palette.Green500 else SpawnTheme.colors.divider))
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 8.dp)) {
            if (onBack != null) {
                IconButton(onBack, Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SpawnTheme.colors.heading) }
            }
            Text(title, style = onest(20.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, modifier = Modifier.align(Alignment.Center))
        }
        if (subtitle != null) {
            Text(subtitle, style = onest(14.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// region Step 1: type grid
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TypeStep(vm: CreateActivityViewModel, bottomPadding: Dp) {
    var menuFor by remember { mutableStateOf<ActivityTypeDTO?>(null) }
    var editing by remember { mutableStateOf<ActivityTypeDTO?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<ActivityTypeDTO?>(null) }
    val haptics = LocalHapticFeedback.current
    Column(Modifier.fillMaxSize().padding(bottom = bottomPadding)) {
        StepHeader("What are you up to?", onBack = null)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            items(vm.types, key = { it.id }) { t ->
                Box {
                    Column(
                        Modifier
                            .aspectRatio(0.95f)
                            .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(SpawnTheme.colors.typeButton)
                            .alpha(if (menuFor != null && menuFor?.id != t.id) 0.4f else 1f)
                            .combinedClickable(onClick = { vm.pickType(t) }, onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); menuFor = t }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(t.icon, fontSize = 30.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(t.title, style = onest(15.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 6.dp))
                        Text("${t.associatedFriends.size} people", style = onest(12.sp), color = SpawnTheme.colors.textSecondary)
                    }
                    if (t.isPinned) {
                        Box(Modifier.padding(6.dp).size(20.dp).clip(CircleShape).background(Palette.Red600), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PushPin, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                    DropdownMenu(menuFor?.id == t.id, { menuFor = null }) {
                        DropdownMenuItem({ Text(if (t.isPinned) "Unpin Type" else "Pin Type") }, trailingIcon = { Icon(Icons.Outlined.PushPin, null) }, onClick = { menuFor = null; vm.togglePin(t) })
                        DropdownMenuItem({ Text("Manage Type") }, trailingIcon = { Icon(Icons.Outlined.Tune, null) }, onClick = { menuFor = null; editing = t })
                        DropdownMenuItem({ Text("Delete Type", color = Palette.Red600) }, trailingIcon = { Icon(Icons.Outlined.Delete, null, tint = Palette.Red600) }, onClick = { menuFor = null; confirmDelete = t })
                    }
                }
            }
            item(key = "new") {
                Column(
                    Modifier
                        .aspectRatio(0.95f)
                        .clip(RoundedCornerShape(14.dp))
                        .drawBehind {
                            drawRoundRect(
                                Palette.Gray300, style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
                                cornerRadius = CornerRadius(14.dp.toPx()),
                            )
                        }
                        .clickable { creating = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Palette.Green500), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Create New\nActivity", style = onest(13.sp, FontWeight.Medium), color = SpawnTheme.colors.text, textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Press & hold to edit", style = onest(13.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
    if (creating) TypeEditor(null, vm, onDismiss = { creating = false })
    editing?.let { t -> TypeEditor(t, vm, onDismiss = { editing = null }) }
    confirmDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete “${t.title}”?") },
            text = { Text("This removes the activity type for you. Activities you already made stay.") },
            confirmButton = { TextButton({ vm.deleteType(t); confirmDelete = null }) { Text("Delete", color = Palette.Red600) } },
            dismissButton = { TextButton({ confirmDelete = null }) { Text("Cancel") } },
        )
    }
}

/** Create or manage an activity type: emoji, name and the friends it invites by default. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeEditor(existing: ActivityTypeDTO?, vm: CreateActivityViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "⭐️") }
    var people by remember { mutableStateOf(existing?.associatedFriends?.map { it.id }?.toSet() ?: vm.friends.map { it.id }.toSet()) }
    val emojis = listOf("⭐️", "🍽️", "☕️", "🏀", "🎮", "📚", "🎬", "🍻", "🏃", "🧗", "🎤", "🛍️", "🏖️", "🎲", "🍕", "🚗")
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SpawnTheme.colors.background) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState())) {
            Text(if (existing == null) "Create Activity Type" else "Manage Type", style = onest(20.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Box(Modifier.align(Alignment.CenterHorizontally).size(84.dp).clip(RoundedCornerShape(18.dp)).background(SpawnTheme.colors.typeButton), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 40.sp)
            }
            Spacer(Modifier.height(12.dp))
            LazyRowEmojis(emojis, icon) { icon = it }
            Spacer(Modifier.height(16.dp))
            SpawnTextField(title, { title = it.take(30) }, "e.g. Basketball", label = "Name")
            Spacer(Modifier.height(16.dp))
            Text("Invite by default (${people.size})", style = onest(14.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
            Spacer(Modifier.height(6.dp))
            if (vm.friends.isEmpty()) Text("Add friends to invite them by default.", style = onest(14.sp), color = SpawnTheme.colors.textSecondary)
            vm.friends.forEach { f ->
                Row(Modifier.fillMaxWidth().clickable { people = if (f.id in people) people - f.id else people + f.id }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(f.profilePicture, f.name ?: f.username, 36.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.name ?: f.username ?: "", style = onest(15.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
                        Text("@${f.username}", style = onest(12.sp), color = SpawnTheme.colors.textSecondary)
                    }
                    Checkbox(f.id in people, { people = if (it) people + f.id else people - f.id }, colors = CheckboxDefaults.colors(checkedColor = Palette.Indigo500))
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(if (existing == null) "Create Type" else "Save", {
                if (existing == null) vm.createType(title, icon, people) else vm.updateType(existing, title, icon, people)
                onDismiss()
            }, enabled = title.isNotBlank())
        }
    }
}

@Composable
private fun LazyRowEmojis(emojis: List<String>, selected: String, onPick: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(emojis) { e ->
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (e == selected) Palette.Indigo100 else SpawnTheme.colors.input)
                    .border(if (e == selected) 1.5.dp else 0.dp, if (e == selected) Palette.Indigo500 else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { onPick(e) },
                contentAlignment = Alignment.Center,
            ) { Text(e, fontSize = 22.sp) }
        }
    }
}
// endregion

// region Step 2: time
@Composable
private fun TimeStep(vm: CreateActivityViewModel, bottomPadding: Dp) {
    Column(Modifier.fillMaxSize().padding(bottom = bottomPadding).imePadding()) {
        StepHeader("What time?", "Set a time for your “${vm.selectedType?.title ?: "new"}” Activity", onBack = { vm.step = CreateStep.Type })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(SpawnTheme.colors.surfaceMuted))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    WheelPicker(listOf("Today", "Tomorrow"), vm.dayOffset, { vm.dayOffset = it }, Modifier.width(120.dp))
                    WheelPicker((1..12).map { "$it" }, vm.hour12 - 1, { vm.hour12 = it + 1 }, Modifier.width(56.dp))
                    WheelPicker(listOf("00", "15", "30", "45"), vm.minute / 15, { vm.minute = it * 15 }, Modifier.width(56.dp))
                    WheelPicker(listOf("AM", "PM"), if (vm.pm) 1 else 0, { vm.pm = it == 1 }, Modifier.width(64.dp))
                }
            }
            if (!vm.timeIsValid()) {
                Text("That time has already passed today.", style = onest(13.sp), color = Palette.Red600, modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(Modifier.height(20.dp))
            SpawnTextField(vm.title, { vm.title = it.take(60) }, "Enter Activity Title", label = "Title")
            Spacer(Modifier.height(18.dp))
            Text("Activity Duration", style = onest(14.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Duration.entries.forEach { d -> Chip(d.label, vm.duration == d, Modifier.weight(1f)) { vm.duration = d } }
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton("Next Step (Location)", { vm.step = CreateStep.Location }, enabled = vm.timeIsValid())
            Spacer(Modifier.height(12.dp))
            StepIndicator(0)
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.2.dp, if (selected) Palette.Indigo500 else SpawnTheme.colors.divider, RoundedCornerShape(10.dp))
            .background(if (selected) Palette.Indigo100.copy(alpha = if (SpawnTheme.colors.isDark) 0.15f else 1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = onest(13.sp, if (selected) FontWeight.SemiBold else FontWeight.Medium), color = if (selected) Palette.Indigo500 else SpawnTheme.colors.text, maxLines = 1)
    }
}

/** iOS-style wheel: snaps, the centered row is the selection. */
@Composable
fun WheelPicker(items: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val rowH = 40.dp
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val fling = rememberSnapFlingBehavior(state)
    val center by remember { derivedStateOf { state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > 60) 1 else 0 } }
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            val idx = center.coerceIn(0, items.lastIndex)
            if (idx != selected) onSelect(idx)
            state.animateScrollToItem(idx)
        }
    }
    LaunchedEffect(selected) { if (!state.isScrollInProgress && state.firstVisibleItemIndex != selected) state.scrollToItem(selected) }
    LazyColumn(
        state = state, flingBehavior = fling, modifier = modifier.height(rowH * 5),
        contentPadding = PaddingValues(vertical = rowH * 2), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(items) { i, s ->
            val dist = kotlin.math.abs(i - center)
            Box(Modifier.height(rowH).fillMaxWidth().clickable { onSelect(i) }, contentAlignment = Alignment.Center) {
                Text(
                    s, style = onest(if (dist == 0) 22.sp else 17.sp, if (dist == 0) FontWeight.Medium else FontWeight.Normal),
                    color = SpawnTheme.colors.text.copy(alpha = when (dist) { 0 -> 1f; 1 -> 0.55f; else -> 0.25f }),
                )
            }
        }
    }
}
// endregion

// region Step 3: location
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationStep(vm: CreateActivityViewModel, bottomPadding: Dp) {
    val context = LocalContext.current
    val me by UserLocation.location.collectAsState()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var pinMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val start = me?.let { Point.fromLngLat(it.longitude, it.latitude) } ?: Point.fromLngLat(-123.2460, 49.2606)
    val viewport = rememberMapViewportState { setCameraOptions { center(start); zoom(14.5) } }

    LaunchedEffect(Unit) { UserLocation.refresh(context) }
    LaunchedEffect(me) {
        me?.let { viewport.setCameraOptions { center(Point.fromLngLat(it.longitude, it.latitude)); zoom(14.5) } }
    }
    LaunchedEffect(query, me) {
        delay(300)
        results = if (query.isBlank()) {
            // Nearby suggestions when nothing's typed yet
            PlaceSearch.nearby(context, me)
        } else PlaceSearch.search(context, query, me)
    }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(Modifier.fillMaxSize(), mapViewportState = viewport, scaleBar = {}, compass = {})
        if (pinMode) {
            Icon(Icons.Filled.Place, null, tint = Palette.Indigo600, modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp).size(44.dp))
            IconButton({ pinMode = false }, Modifier.statusBarsPadding().padding(12.dp).clip(CircleShape).background(Color.White)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Palette.FigmaNavy)
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(SpawnTheme.colors.background)
                    .padding(bottom = bottomPadding).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Set Location", style = onest(18.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading)
                Text("Drag map to move pin", style = onest(13.sp), color = SpawnTheme.colors.textSecondary)
                Spacer(Modifier.height(14.dp))
                PrimaryButton("Confirm Location", {
                    val c = viewport.cameraState?.center ?: start
                    scope.launch {
                        vm.location = PlaceSearch.reverse(context, c.latitude(), c.longitude()).toLocation()
                        vm.step = CreateStep.Confirm
                    }
                })
                Spacer(Modifier.height(10.dp))
                StepIndicator(1)
            }
        } else {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.72f)
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(SpawnTheme.colors.background)
                    .imePadding().padding(bottom = bottomPadding),
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(40.dp, 5.dp).clip(CircleShape).background(SpawnTheme.colors.divider))
                }
                Box(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp)) {
                    IconButton({ vm.step = CreateStep.Time }, Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SpawnTheme.colors.heading) }
                    Text("Choose Location", style = onest(18.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, modifier = Modifier.align(Alignment.Center))
                }
                SpawnTextField(query, { query = it }, "Where at?", Modifier.padding(horizontal = 16.dp))
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    item {
                        PlaceRow(Icons.Outlined.NearMe, "Current Location", if (me != null) "Use where you are now" else "Location is off", null) {
                            me?.let { l -> scope.launch { vm.location = PlaceSearch.reverse(context, l.latitude, l.longitude).copy(name = "Current Location").toLocation().copy(name = "Current Location"); vm.step = CreateStep.Confirm } }
                        }
                    }
                    items(results) { p ->
                        val dist = Distance.between(me, p.toLocation())?.removeSuffix(" away")
                        PlaceRow(Icons.Outlined.Place, p.name, p.address, dist) { vm.location = p.toLocation(); vm.step = CreateStep.Confirm }
                    }
                    item { PlaceRow(Icons.Outlined.PushPin, "Set Location on Map", null, null) { pinMode = true } }
                }
                Box(Modifier.padding(12.dp)) { StepIndicator(1) }
            }
        }
    }
}

@Composable
private fun PlaceRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String?, distance: String?, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = SpawnTheme.colors.text, modifier = Modifier.size(20.dp))
                if (distance != null) Text(distance, style = onest(10.sp), color = SpawnTheme.colors.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = onest(15.sp, FontWeight.Medium), color = SpawnTheme.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrBlank()) Text(subtitle, style = onest(12.sp), color = SpawnTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        HorizontalDivider(color = SpawnTheme.colors.divider)
    }
}
// endregion

// region Step 4: confirm
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmStep(vm: CreateActivityViewModel, bottomPadding: Dp) {
    var customOpen by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf("") }
    var inviteOpen by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    Column(Modifier.fillMaxSize().padding(bottom = bottomPadding).imePadding()) {
        StepHeader("Confirm", onBack = { vm.step = CreateStep.Location })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier.size(110.dp).shadow(3.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(SpawnTheme.colors.typeButton),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Text(vm.selectedType?.icon ?: "⭐️", fontSize = 34.sp)
                Text(vm.selectedType?.title ?: "Activity", style = onest(15.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading)
                Text("${vm.invited.size} people", style = onest(12.sp), color = SpawnTheme.colors.textSecondary)
            }
            Spacer(Modifier.height(18.dp))
            Text(vm.effectiveTitle, style = onest(26.sp, FontWeight.Bold), color = SpawnTheme.colors.heading, textAlign = TextAlign.Center)
            Text(
                "${Distance.shortLocation(vm.location?.name)} @ ${timeFmt.format(vm.startLocal)}${if (vm.dayOffset == 1) " tomorrow" else ""}",
                style = onest(15.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            Text("How many people can join?", style = onest(14.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Any amount!", vm.participantLimit == null) { vm.participantLimit = null }
                Chip("10", vm.participantLimit == 10) { vm.participantLimit = 10 }
                Chip("50", vm.participantLimit == 50) { vm.participantLimit = 50 }
                val isCustom = vm.participantLimit != null && vm.participantLimit !in listOf(10, 50)
                Chip(if (isCustom) "Custom: ${vm.participantLimit}" else "Custom", isCustom) { customOpen = true }
            }
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SpawnTheme.colors.input).clickable { inviteOpen = true }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.GroupAdd, null, tint = Palette.Indigo500)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Who’s invited", style = onest(15.sp, FontWeight.SemiBold), color = SpawnTheme.colors.text)
                    val names = vm.friends.filter { it.id in vm.invited }.map { it.name?.split(" ")?.first() ?: it.username ?: "" }
                    Text(
                        if (names.isEmpty()) "Nobody yet — tap to pick friends" else names.take(4).joinToString(", ") + if (names.size > 4) " +${names.size - 4}" else "",
                        style = onest(13.sp), color = SpawnTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = SpawnTheme.colors.textSecondary)
            }
            Spacer(Modifier.height(12.dp))
            SpawnTextField(vm.note, { vm.note = it.take(200) }, "Add a note (optional)", singleLine = false, minLines = 2)
            vm.error?.let { Spacer(Modifier.height(10.dp)); Text(it, style = onest(14.sp), color = Palette.Red600) }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton("Looks good to me!", vm::submit, loading = vm.submitting)
            Spacer(Modifier.height(12.dp))
            StepIndicator(2)
        }
    }
    if (customOpen) {
        AlertDialog(
            onDismissRequest = { customOpen = false },
            title = { Text("Max participants") },
            text = { OutlinedTextField(custom, { custom = it.filter(Char::isDigit).take(4) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) },
            confirmButton = { TextButton({ custom.toIntOrNull()?.takeIf { it > 0 }?.let { vm.participantLimit = it }; customOpen = false }) { Text("Done") } },
            dismissButton = { TextButton({ customOpen = false }) { Text("Cancel") } },
        )
    }
    if (inviteOpen) {
        ModalBottomSheet(onDismissRequest = { inviteOpen = false }, containerColor = SpawnTheme.colors.background) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invite friends", style = onest(20.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, modifier = Modifier.weight(1f))
                    TextButton({ vm.invited = if (vm.invited.size == vm.friends.size) emptySet() else vm.friends.map { it.id }.toSet() }) {
                        Text(if (vm.invited.size == vm.friends.size) "Clear" else "Select all", color = Palette.Indigo500)
                    }
                }
                if (vm.friends.isEmpty()) Text("Add friends from the Friends tab to invite them.", style = onest(14.sp), color = SpawnTheme.colors.textSecondary)
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(vm.friends, key = { it.id }) { f ->
                        Row(Modifier.fillMaxWidth().clickable { vm.invited = if (f.id in vm.invited) vm.invited - f.id else vm.invited + f.id }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(f.profilePicture, f.name ?: f.username, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(f.name ?: f.username ?: "", style = onest(15.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
                                Text("@${f.username}", style = onest(12.sp), color = SpawnTheme.colors.textSecondary)
                            }
                            Checkbox(f.id in vm.invited, null, colors = CheckboxDefaults.colors(checkedColor = Palette.Indigo500))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Done", { inviteOpen = false })
            }
        }
    }
}
// endregion

// region Step 5: success
@Composable
private fun SuccessStep(vm: CreateActivityViewModel, bottomPadding: Dp, onDone: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(bottom = bottomPadding).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(130.dp).border(10.dp, Palette.Green500, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = Palette.Green500, modifier = Modifier.size(84.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("Success!", style = onest(30.sp, FontWeight.Bold), color = SpawnTheme.colors.heading)
        Spacer(Modifier.height(8.dp))
        Text(
            "You’ve spawned in and “${vm.created?.title ?: vm.effectiveTitle}” is now live for your friends.",
            style = onest(16.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            "Share with your network", { vm.created?.let { shareActivity(context, it) } },
            brush = Brush.horizontalGradient(listOf(Color(0xFF29C5E6), Color(0xFF3D7BF2))), leading = Icons.Outlined.IosShare,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(14.dp))
        OutlinePillButton("Return to Home", onDone, color = SpawnTheme.colors.text)
    }
}
// endregion
