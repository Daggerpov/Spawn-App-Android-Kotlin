package com.danielagapov.spawn.ui.friends

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.BuildConfig
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.ui.activity.ReportDialog
import com.danielagapov.spawn.ui.components.EmptyState
import com.danielagapov.spawn.ui.components.ErrorBanner
import com.danielagapov.spawn.ui.components.LoadingBox
import com.danielagapov.spawn.ui.components.UserAvatar
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

private enum class FriendsPage { Main, Search, Requests }

/** Friends tab (Figma "Friends List - Draft 1"): requests banner, search, your friends, recently spawned with. */
@Composable
fun FriendsTab(vm: FriendsViewModel, onOpenProfile: (BaseUserDTO) -> Unit, bottomPadding: Dp) {
    var page by rememberSaveable { mutableStateOf(FriendsPage.Main) }
    val context = LocalContext.current
    LaunchedEffect(vm.toast) { vm.toast?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.toast = null } }
    LaunchedEffect(Unit) { vm.load() }
    BackHandler(page != FriendsPage.Main) { vm.clearSearch(); page = FriendsPage.Main }

    AnimatedContent(page, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "friends") { p ->
        when (p) {
            FriendsPage.Main -> FriendsMain(vm, onOpenProfile, bottomPadding, onSearch = { page = FriendsPage.Search }, onRequests = { page = FriendsPage.Requests })
            FriendsPage.Search -> FriendSearch(vm, onOpenProfile, bottomPadding, onBack = { vm.clearSearch(); page = FriendsPage.Main })
            FriendsPage.Requests -> FriendRequestsPage(vm, onOpenProfile, bottomPadding, onBack = { page = FriendsPage.Main })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsMain(
    vm: FriendsViewModel,
    onOpenProfile: (BaseUserDTO) -> Unit,
    bottomPadding: Dp,
    onSearch: () -> Unit,
    onRequests: () -> Unit,
) {
    var showAllFriends by rememberSaveable { mutableStateOf(false) }
    var showAllRecent by rememberSaveable { mutableStateOf(false) }
    PullToRefreshBox(isRefreshing = false, onRefresh = { vm.load() }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { RequestsBanner(vm.incoming.size, onRequests) }
            item { Spacer(Modifier.height(8.dp)); SearchBarButton(onSearch); Spacer(Modifier.height(8.dp)) }
            vm.error?.let { e -> item { ErrorBanner(e) { vm.error = null; vm.load() } } }

            if (vm.loading && vm.friends.isEmpty()) {
                item { LoadingBox() }
            } else {
                item { ListHeader("Your Friends (${vm.friends.size})", if (vm.friends.size > 5) (if (showAllFriends) "Show Less" else "Show All") else null) { showAllFriends = !showAllFriends } }
                if (vm.friends.isEmpty()) {
                    item {
                        EmptyState("👋", "No friends yet", "Search for people you know or add someone you've spawned with.")
                    }
                }
                items(if (showAllFriends) vm.friends else vm.friends.take(5), key = { "f" + it.id }) { f ->
                    val u = f.toBase()
                    UserRow(u, onClick = { onOpenProfile(u) }) { FriendMenu(vm, u, onOpenProfile) }
                }

                val recent = vm.recentNonFriends
                if (recent.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)); ListHeader("Recently Spawned With", if (recent.size > 3) (if (showAllRecent) "Show Less" else "Show All") else null) { showAllRecent = !showAllRecent } }
                    items(if (showAllRecent) recent else recent.take(3), key = { "r" + it.id }) { u ->
                        UserRow(u, onClick = { onOpenProfile(u) }) { AddButton(vm, u.id) }
                    }
                }
                val suggestions = vm.suggestions
                if (suggestions.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)); ListHeader("Suggested Friends", null) {} }
                    items(suggestions.take(8), key = { "s" + it.id }) { r ->
                        val u = r.toBase()
                        UserRow(u, subtitle = r.mutualFriendCount?.takeIf { it > 0 }?.let { "${u.handle} • $it mutual" }, onClick = { onOpenProfile(u) }) { AddButton(vm, u.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestsBanner(count: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Indigo500)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Friend Requests", style = onest(17.sp, FontWeight.SemiBold), color = Color.White)
        if (count > 0) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.defaultMinSize(20.dp, 20.dp).clip(CircleShape).background(Palette.Red500).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                Text("$count", style = onest(12.sp, FontWeight.SemiBold), color = Color.White)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("View All >", style = onest(16.sp, FontWeight.SemiBold), color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun SearchBarButton(onClick: () -> Unit) {
    val c = SpawnTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, c.text, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = c.text, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Search for friends...", style = onest(16.sp, FontWeight.Medium), color = c.text)
    }
}

@Composable
private fun ListHeader(title: String, action: String?, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(title, style = onest(16.sp, FontWeight.Medium), color = SpawnTheme.colors.textSecondary, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, style = onest(14.sp, FontWeight.Medium), color = Palette.Indigo600, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

/** Figma "UserListItem": 36dp avatar, name + @handle, trailing control. */
@Composable
fun UserRow(user: BaseUserDTO, onClick: () -> Unit, subtitle: String? = null, trailing: @Composable RowScope.() -> Unit = {}) {
    val c = SpawnTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(user, 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.displayName, style = onest(14.sp, FontWeight.SemiBold), color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle ?: user.handle, style = onest(12.sp), color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** Small rounded-8 button from the Figma list items ("Add +", "Accept", "Remove"). */
@Composable
fun SmallButton(text: String, onClick: () -> Unit, filled: Boolean = false, enabled: Boolean = true) {
    val c = SpawnTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .clip(shape)
            .then(if (filled) Modifier.background(if (enabled) Color(0xFF242CBB) else Palette.Gray300) else Modifier.border(1.dp, if (enabled) c.textSecondary else c.placeholder, shape))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = onest(14.sp, FontWeight.Medium), color = if (filled) Color.White else if (enabled) c.textSecondary else c.placeholder)
    }
}

@Composable
private fun AddButton(vm: FriendsViewModel, userId: String) {
    when (vm.relationshipOf(userId)) {
        UserRelationshipType.FRIEND -> SmallButton("Friends", {}, enabled = false)
        UserRelationshipType.OUTGOING_FRIEND_REQUEST -> SmallButton("Requested", {}, enabled = false)
        UserRelationshipType.INCOMING_FRIEND_REQUEST -> vm.incomingRequestFrom(userId)?.let { r -> SmallButton("Accept", { vm.accept(r) }, filled = true) }
        else -> SmallButton("Add +", { vm.sendRequest(userId) })
    }
}

/** The ⋯ menu on a friend row (Figma "Friends List - User Menu"). */
@Composable
private fun FriendMenu(vm: FriendsViewModel, user: BaseUserDTO, onOpenProfile: (BaseUserDTO) -> Unit) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    var confirmBlock by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.MoreHoriz, "More", tint = SpawnTheme.colors.text)
        }
        UserActionsMenu(
            expanded = open,
            onDismiss = { open = false },
            user = user,
            isFriend = true,
            onViewProfile = { onOpenProfile(user) },
            onRemove = { confirmRemove = true },
            onReport = { reporting = true },
            onBlock = { confirmBlock = true },
            context = context,
        )
    }
    if (confirmRemove) ConfirmDialog("Remove ${user.firstName} as a friend?", "You can always add them back later.", "Remove", { confirmRemove = false }) {
        confirmRemove = false; vm.removeFriend(user.id)
    }
    if (confirmBlock) ConfirmDialog("Block ${user.firstName}?", "They won't be able to see your activities or find you, and they'll be removed from your friends.", "Block", { confirmBlock = false }) {
        confirmBlock = false; vm.block(user.id, "Blocked from friends list")
    }
    if (reporting) ReportDialog("Report ${user.firstName}", onDismiss = { reporting = false }) { type, desc ->
        reporting = false; vm.report(user.id, type, desc)
    }
}

@Composable
fun UserActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    user: BaseUserDTO,
    isFriend: Boolean,
    context: Context,
    onViewProfile: (() -> Unit)? = null,
    onAddToType: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    onReport: () -> Unit,
    onBlock: () -> Unit,
) {
    DropdownMenu(expanded, onDismiss, shape = RoundedCornerShape(12.dp)) {
        @Composable
        fun item(label: String, danger: Boolean = false, action: () -> Unit) = DropdownMenuItem(
            text = { Text(label, style = onest(15.sp, FontWeight.Medium), color = if (danger) Palette.Red600 else SpawnTheme.colors.text) },
            onClick = { onDismiss(); action() },
        )
        onViewProfile?.let { item("View profile", action = it) }
        if (isFriend) onAddToType?.let { item("Add to Activity Type", action = it) }
        if (isFriend) onRemove?.let { item("Remove as friend", action = it) }
        item("Copy profile URL") { copyProfileUrl(context, user) }
        item("Share this Profile") { shareProfile(context, user) }
        HorizontalDivider()
        item("Report user", danger = true, action = onReport)
        item("Block user", danger = true, action = onBlock)
    }
}

@Composable
fun ConfirmDialog(title: String, message: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = onest(18.sp, FontWeight.SemiBold)) },
        text = { Text(message, style = onest(15.sp)) },
        confirmButton = { TextButton(onConfirm) { Text(confirm, color = Palette.Red600, style = onest(15.sp, FontWeight.SemiBold)) } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", style = onest(15.sp)) } },
    )
}

fun profileUrl(user: BaseUserDTO) = "${BuildConfig.SHARE_BASE_URL}/profile/${user.id}"

fun copyProfileUrl(context: Context, user: BaseUserDTO) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Spawn profile", profileUrl(user)))
    Toast.makeText(context, "Profile link copied", Toast.LENGTH_SHORT).show()
}

fun shareProfile(context: Context, user: BaseUserDTO) {
    val text = "Check out ${user.displayName} (${user.handle}) on Spawn! ${profileUrl(user)}"
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
    }, "Share profile"))
}

// region Search (Figma "Friends List - Search" / "Empty Search")
@Composable
private fun FriendSearch(vm: FriendsViewModel, onOpenProfile: (BaseUserDTO) -> Unit, bottomPadding: Dp, onBack: () -> Unit) {
    val c = SpawnTheme.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading) }
            Row(
                Modifier
                    .weight(1f)
                    .offset(x = (-8).dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, c.text, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, null, tint = c.text, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (vm.query.isEmpty()) Text("Search for friends...", style = onest(16.sp, FontWeight.Medium), color = c.placeholder)
                    BasicTextField(
                        vm.query, vm::onQueryChange,
                        singleLine = true,
                        textStyle = onest(16.sp, FontWeight.Medium).copy(color = c.text),
                        cursorBrush = SolidColor(Palette.Indigo500),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    )
                }
                if (vm.query.isNotEmpty()) {
                    Icon(Icons.Default.Close, "Clear", tint = c.textSecondary, modifier = Modifier.size(18.dp).clickable { vm.clearSearch() })
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (vm.query.isBlank()) {
                val recent = vm.recentNonFriends.ifEmpty { vm.suggestions.map { it.toBase() } }
                if (recent.isNotEmpty()) {
                    item { ListHeader(if (vm.recentNonFriends.isNotEmpty()) "Recently Spawned With" else "Suggested Friends", null) {} }
                    items(recent, key = { "r" + it.id }) { u -> UserRow(u, onClick = { onOpenProfile(u) }) { AddButton(vm, u.id) } }
                }
            } else {
                val friendIds = vm.friends.map { it.id }.toSet()
                val others = vm.results.filter { it.user.id !in friendIds }
                if (vm.searching && vm.results.isEmpty()) item { LoadingBox() }
                items(others, key = { "x" + it.user.id }) { r ->
                    UserRow(r.user, subtitle = r.mutualFriendCount?.takeIf { it > 0 }?.let { "${r.user.handle} • $it mutual" }, onClick = { onOpenProfile(r.user) }) {
                        AddButton(vm, r.user.id)
                    }
                }
                val matching = vm.friendsMatching(vm.query)
                if (matching.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)); ListHeader("Your Friends (${matching.size})", null) {} }
                    items(matching, key = { "f" + it.id }) { f ->
                        val u = f.toBase()
                        UserRow(u, onClick = { onOpenProfile(u) }) { FriendMenu(vm, u, onOpenProfile) }
                    }
                }
                if (!vm.searching && others.isEmpty() && matching.isEmpty()) {
                    item { EmptyState("🔍", "No one found", "Try a different name or username.") }
                }
            }
        }
    }
}
// endregion

// region Friend requests (Figma "Friend Requests")
@Composable
private fun FriendRequestsPage(vm: FriendsViewModel, onOpenProfile: (BaseUserDTO) -> Unit, bottomPadding: Dp, onBack: () -> Unit) {
    val c = SpawnTheme.colors
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading) }
            Text("Friend Requests", style = onest(20.sp, FontWeight.SemiBold), color = c.heading, modifier = Modifier.align(Alignment.Center))
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (vm.incoming.isEmpty() && vm.sent.isEmpty()) {
                item { EmptyState("📭", "No friend requests", "When someone adds you, you'll see it here.") }
            }
            if (vm.incoming.isNotEmpty()) {
                item { ListHeader("Received (${vm.incoming.size})", null) {} }
                items(vm.incoming, key = { "i" + it.id }) { r ->
                    UserRow(
                        r.senderUser,
                        subtitle = r.mutualFriendCount?.takeIf { it > 0 }?.let { "${r.senderUser.handle} • $it mutual" },
                        onClick = { onOpenProfile(r.senderUser) },
                    ) {
                        SmallButton("Accept", { vm.accept(r) }, filled = true)
                        SmallButton("Remove", { vm.reject(r) })
                    }
                }
            }
            if (vm.sent.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); ListHeader("Sent (${vm.sent.size})", null) {} }
                items(vm.sent, key = { "s" + it.id }) { r ->
                    UserRow(r.receiverUser, onClick = { onOpenProfile(r.receiverUser) }) {
                        SmallButton("Cancel", { vm.cancel(r) })
                    }
                }
            }
        }
    }
}
// endregion
