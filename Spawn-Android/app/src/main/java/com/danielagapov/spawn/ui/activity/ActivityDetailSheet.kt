package com.danielagapov.spawn.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danielagapov.spawn.BuildConfig
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.ActivityStatus
import com.danielagapov.spawn.core.util.Dates
import com.danielagapov.spawn.core.util.Distance
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.ui.components.*
import com.danielagapov.spawn.ui.theme.ActivityPalette
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.onest

private enum class Page { Detail, People, Chat }

/**
 * The activity drawer from Figma ("Event (Drawer=Open)"): gradient sheet with the activity, Spawn In!/Going
 * button, mini map, chatroom preview; plus the "Who's Coming?" and Chatroom pages inside the same sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailSheet(
    initial: FullFeedActivityDTO,
    onDismiss: () -> Unit,
    onOpenProfile: (BaseUserDTO) -> Unit,
    startInChat: Boolean = false,
    vm: ActivityDetailViewModel = viewModel(key = "activity-${initial.id}"),
) {
    LaunchedEffect(initial.id) { vm.open(initial) }
    val a = vm.activity ?: initial
    val colorIndex = Spawn.colors.indexFor(a.id)
    val context = LocalContext.current
    var page by remember { mutableStateOf(if (startInChat) Page.Chat else Page.Detail) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(vm.message) {
        vm.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.message = null }
    }
    DisposableEffect(Unit) { onDispose { vm.stopChatPolling() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colorStops = ActivityPalette.stops(colorIndex),
                            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                            radius = size.height,
                        )
                    )
                },
        ) {
            Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(40.dp, 5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)))
                }
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        else slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    },
                    label = "page",
                ) { p ->
                    when (p) {
                        Page.Detail -> DetailPage(a, vm, colorIndex, onClose = onDismiss, onPeople = { page = Page.People }, onChat = { page = Page.Chat }, onDeleted = onDismiss)
                        Page.People -> PeoplePage(a, vm, onBack = { page = Page.Detail }, onOpenProfile = onOpenProfile)
                        Page.Chat -> ChatPage(a, vm, colorIndex, onBack = { page = Page.Detail })
                    }
                }
            }
        }
        BackHandler(enabled = page != Page.Detail) { page = Page.Detail }
    }
}

fun shareActivity(context: Context, a: FullFeedActivityDTO) {
    val url = "${BuildConfig.SHARE_BASE_URL}/activity/${a.id}"
    val text = "Join me for ${a.title ?: "an activity"} on Spawn! $url"
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
    }, "Share activity"))
}

fun openInMaps(context: Context, loc: LocationDTO) {
    val uri = Uri.parse("geo:${loc.latitude},${loc.longitude}?q=${loc.latitude},${loc.longitude}(${Uri.encode(loc.name)})")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${loc.latitude},${loc.longitude}")))
    }
}

fun staticMapUrl(context: Context, loc: LocationDTO, w: Int = 600, h: Int = 280, zoom: Double = 14.5): String {
    val token = context.getString(R.string.mapbox_access_token).trim()
    val lng = loc.longitude; val lat = loc.latitude
    return "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/pin-l+fd4e4c($lng,$lat)/$lng,$lat,$zoom,0/${w}x$h@2x?access_token=$token"
}

@Composable
private fun DetailPage(
    a: FullFeedActivityDTO,
    vm: ActivityDetailViewModel,
    colorIndex: Int,
    onClose: () -> Unit,
    onPeople: () -> Unit,
    onChat: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val loc by UserLocation.location.collectAsState()
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var reportOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    val accent = ActivityPalette.color(colorIndex)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.Filled.Close, "Close", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreHoriz, "More", tint = Color.White) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("Share") }, leadingIcon = { Icon(Icons.Outlined.IosShare, null) }, onClick = { menu = false; shareActivity(context, a) })
                    if (vm.isOwner) {
                        DropdownMenuItem({ Text("Edit") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menu = false; editing = true })
                        DropdownMenuItem({ Text("Delete", color = Palette.Red600) }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Palette.Red600) }, onClick = { menu = false; confirmDelete = true })
                    } else {
                        DropdownMenuItem({ Text("Report activity", color = Palette.Red600) }, leadingIcon = { Icon(Icons.Outlined.Flag, null, tint = Palette.Red600) }, onClick = { menu = false; reportOpen = true })
                    }
                }
            }
        }
        Text(a.title ?: "${a.creatorUser.firstName}'s activity", style = onest(28.sp, FontWeight.Bold), color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(Dates.relativeAndRange(a), style = onest(15.sp, FontWeight.Medium), color = Color.White.copy(alpha = 0.9f))
        if (!a.note.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("“${a.note}”", style = onest(15.sp), color = Color.White.copy(alpha = 0.9f))
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val going = vm.isGoing
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable(enabled = !vm.isOwner) { vm.toggleParticipation() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (vm.toggling) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = accent, strokeWidth = 2.dp)
                } else {
                    Icon(
                        if (going) Icons.Outlined.CheckCircle else Icons.Outlined.StarOutline, null,
                        tint = if (going) Palette.Green600 else accent, modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when { vm.isOwner -> "Hosting"; going -> "Going"; vm.isFull -> "Full"; else -> "Spawn In!" },
                        style = onest(17.sp, FontWeight.SemiBold), color = if (going) Palette.Green600 else accent,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Box(Modifier.clickable(onClick = onPeople)) {
                val people = listOf(a.creatorUser) + a.participantUsers.orEmpty().filterNot { it.id == a.creatorUser.id }
                ParticipantsStack(people, size = 42.dp, max = 3)
            }
        }
        Spacer(Modifier.height(18.dp))
        a.location?.let { l ->
            Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f))) {
                AsyncImage(
                    model = staticMapUrl(context, l), contentDescription = "Map of ${l.name}", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clickable { openInMaps(context, l) },
                )
                Row(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier.weight(1f, fill = false).clip(RoundedCornerShape(10.dp)).background(accent).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Place, null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            Distance.shortLocation(l.name) + (Distance.between(loc, l)?.let { " • $it" } ?: ""),
                            style = onest(13.sp, FontWeight.SemiBold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(
                        Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White).clickable { openInMaps(context, l) }.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Directions, null, tint = accent, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("View in Maps", style = onest(13.sp, FontWeight.SemiBold), color = accent)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        val last = vm.chats.lastOrNull()
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.12f))
                .clickable(onClick = onChat)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (last != null) {
                val senders = vm.chats.map { it.senderUser }.distinctBy { it.id }.takeLast(2).reversed()
                Box(Modifier.size(44.dp)) {
                    senders.getOrNull(1)?.let { UserAvatar(it, 28.dp, Modifier.align(Alignment.TopEnd)) }
                    UserAvatar(senders.first(), 32.dp, Modifier.align(Alignment.BottomStart), androidx.compose.foundation.BorderStroke(1.5.dp, Color.White))
                }
            } else {
                Box(Modifier.size(44.dp)) {
                    Box(Modifier.size(28.dp).align(Alignment.TopEnd).border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape))
                    Box(Modifier.size(32.dp).align(Alignment.BottomStart).border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Chatroom", style = onest(15.sp, FontWeight.SemiBold), color = Color.White)
                Text(
                    if (last != null) "${last.senderUser.firstName}: ${last.content}" else "Be the first to send a message!",
                    style = onest(15.sp, if (last != null) FontWeight.SemiBold else FontWeight.Normal), color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            if (last != null) LiveDot(Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(32.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to delete this activity? This action cannot be undone.") },
            confirmButton = { TextButton({ confirmDelete = false; vm.delete(onDeleted) }) { Text("Delete", color = Palette.Red600) } },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancel") } },
        )
    }
    if (reportOpen) {
        ReportDialog("Report activity", onDismiss = { reportOpen = false }) { type, desc ->
            reportOpen = false
            vm.report(a.id, EntityType.Activity, type, desc)
        }
    }
    if (editing) {
        EditActivityDialog(a, onDismiss = { editing = false }) { title, note -> vm.update(title, note) { editing = false } }
    }
}

@Composable
fun ReportDialog(title: String, onDismiss: () -> Unit, onSubmit: (ReportType, String) -> Unit) {
    var type by remember { mutableStateOf(ReportType.HARASSMENT) }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                ReportType.entries.forEach { t ->
                    Row(Modifier.fillMaxWidth().clickable { type = t }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(type == t, { type = t })
                        Text(t.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
                OutlinedTextField(desc, { desc = it }, placeholder = { Text("What happened? (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton({ onSubmit(type, desc.ifBlank { type.name }) }) { Text("Report", color = Palette.Red600) } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditActivityDialog(a: FullFeedActivityDTO, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf(a.title ?: "") }
    var note by remember { mutableStateOf(a.note ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit activity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Note") })
            }
        },
        confirmButton = { TextButton({ onSave(title, note) }, enabled = title.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PageHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Text(title, style = onest(20.sp, FontWeight.SemiBold), color = Color.White, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun PeoplePage(a: FullFeedActivityDTO, vm: ActivityDetailViewModel, onBack: () -> Unit, onOpenProfile: (BaseUserDTO) -> Unit) {
    val going = a.participantUsers.orEmpty().filterNot { it.id == a.creatorUser.id }
    val invited = a.invitedUsers.orEmpty().filterNot { u -> going.any { it.id == u.id } || u.id == a.creatorUser.id }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { PageHeader("Who’s Coming?", onBack) }
        item {
            Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
                UserAvatar(a.creatorUser, 64.dp, border = androidx.compose.foundation.BorderStroke(2.dp, Color.White))
                going.getOrNull(0)?.let { UserAvatar(it, 40.dp, Modifier.offset(x = (-56).dp, y = 22.dp)) }
                going.getOrNull(1)?.let { UserAvatar(it, 34.dp, Modifier.offset(x = 52.dp, y = 8.dp)) }
                going.getOrNull(2)?.let { UserAvatar(it, 28.dp, Modifier.offset(x = (-44).dp, y = (-26).dp)) }
            }
        }
        item { SectionLabel("Host") }
        item { PersonRow(a.creatorUser, onOpenProfile, vm) }
        item { SectionLabel("Going (${going.size})") }
        if (going.isEmpty()) item { Text("No one yet — be the first!", style = onest(14.sp), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(horizontal = 22.dp)) }
        items(going, key = { "g" + it.id }) { PersonRow(it, onOpenProfile, vm) }
        if (invited.isNotEmpty()) {
            item { SectionLabel("Invited (${invited.size})") }
            items(invited, key = { "i" + it.id }) { PersonRow(it, onOpenProfile, vm) }
        }
    }
}

@Composable
private fun SectionLabel(t: String) {
    Text(t, style = onest(15.sp, FontWeight.Medium), color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp))
}

@Composable
private fun PersonRow(u: BaseUserDTO, onOpenProfile: (BaseUserDTO) -> Unit, vm: ActivityDetailViewModel) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .padding(horizontal = 22.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.14f))
            .clickable { onOpenProfile(u) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(u, 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(u.displayName, style = onest(16.sp, FontWeight.SemiBold), color = Color.White)
            Text(u.handle, style = onest(13.sp), color = Color.White.copy(alpha = 0.8f))
        }
        if (u.id != vm.me) {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreHoriz, "Options", tint = Color.White) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("View profile") }, onClick = { menu = false; onOpenProfile(u) })
                    DropdownMenuItem({ Text("Report user") }, onClick = { menu = false; vm.report(u.id, EntityType.User, ReportType.HARASSMENT, "Reported from activity") })
                    DropdownMenuItem({ Text("Block", color = Palette.Red600) }, onClick = { menu = false; vm.block(u) })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatPage(a: FullFeedActivityDTO, vm: ActivityDetailViewModel, colorIndex: Int, onBack: () -> Unit) {
    LaunchedEffect(Unit) { vm.startChatPolling() }
    DisposableEffect(Unit) { onDispose { vm.stopChatPolling() } }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(vm.chats.size) { if (vm.chats.isNotEmpty()) listState.animateScrollToItem(vm.chats.size - 1) }
    var menuFor by remember { mutableStateOf<FullActivityChatMessageDTO?>(null) }
    var reportFor by remember { mutableStateOf<FullActivityChatMessageDTO?>(null) }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Chatroom", onBack)
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (vm.chats.isEmpty()) {
                item {
                    Text("Be the first to send a message!", style = onest(15.sp), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.fillMaxWidth().padding(top = 40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            items(vm.chats, key = { it.id }) { m ->
                val mine = m.senderUser.id == vm.me
                val likes = m.likedByUsers.orEmpty().size
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                    if (!mine) Text(m.senderUser.displayName, style = onest(12.sp, FontWeight.Medium), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(start = 40.dp, bottom = 2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (!mine) { UserAvatar(m.senderUser, 30.dp); Spacer(Modifier.width(8.dp)) }
                        Box {
                            Text(
                                m.content,
                                style = onest(15.sp, FontWeight.Medium), color = Palette.FigmaNavy,
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (mine) Color(0xFFE6E9FF) else Color.White.copy(alpha = 0.92f))
                                    .combinedClickable(onClick = {}, onDoubleClick = { vm.toggleLike(m) }, onLongClick = { menuFor = m })
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                            )
                            DropdownMenu(menuFor?.id == m.id, { menuFor = null }) {
                                val liked = m.likedByUsers.orEmpty().any { it.id == vm.me }
                                DropdownMenuItem({ Text(if (liked) "Unlike" else "Like") }, leadingIcon = { Icon(if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null) }, onClick = { menuFor = null; vm.toggleLike(m) })
                                if (mine) {
                                    DropdownMenuItem({ Text("Delete", color = Palette.Red600) }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Palette.Red600) }, onClick = { menuFor = null; vm.deleteMessage(m) })
                                } else {
                                    DropdownMenuItem({ Text("Report") }, leadingIcon = { Icon(Icons.Outlined.Flag, null) }, onClick = { menuFor = null; reportFor = m })
                                    DropdownMenuItem({ Text("Block ${m.senderUser.firstName}", color = Palette.Red600) }, leadingIcon = { Icon(Icons.Outlined.Block, null, tint = Palette.Red600) }, onClick = { menuFor = null; vm.block(m.senderUser) })
                                }
                            }
                        }
                    }
                    if (likes > 0) {
                        Row(
                            Modifier.padding(start = if (mine) 0.dp else 40.dp, top = 2.dp).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Favorite, null, tint = Palette.Red600, modifier = Modifier.size(12.dp))
                            if (likes > 1) Text(" $likes", style = onest(11.sp, FontWeight.SemiBold), color = Palette.FigmaNavy)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Spawn.currentUser?.let { UserAvatar(it, 34.dp) }
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = text, onValueChange = { text = it },
                textStyle = onest(15.sp).copy(color = Palette.FigmaNavy),
                cursorBrush = SolidColor(Palette.Indigo500),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 11.dp),
                decorationBox = { inner -> if (text.isEmpty()) Text("Send a message!", style = onest(15.sp), color = Palette.Gray400); inner() },
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(ActivityPalette.pill(colorIndex)).clickable(enabled = text.isNotBlank()) { vm.send(text); text = "" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
    }
    reportFor?.let { m ->
        ReportDialog("Report message", onDismiss = { reportFor = null }) { type, desc ->
            reportFor = null
            vm.report(m.id, EntityType.ChatMessage, type, desc)
        }
    }
}
