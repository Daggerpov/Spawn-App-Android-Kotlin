package com.danielagapov.spawn.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.activity.ReportDialog
import com.danielagapov.spawn.ui.components.ActivityCard
import com.danielagapov.spawn.ui.components.ErrorBanner
import com.danielagapov.spawn.ui.components.PrimaryButton
import com.danielagapov.spawn.ui.components.UserAvatar
import com.danielagapov.spawn.ui.friends.ConfirmDialog
import com.danielagapov.spawn.ui.friends.UserActionsMenu
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

/** Another user's profile (Figma "View Friends profiles": Unadded / Request Sent / Requested / Friend). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    target: BaseUserDTO,
    onBack: () -> Unit,
    onOpenActivity: (FullFeedActivityDTO) -> Unit,
    bottomPadding: Dp,
    vm: UserProfileViewModel = viewModel(key = "profile-${target.id}"),
) {
    val context = LocalContext.current
    val c = SpawnTheme.colors
    LaunchedEffect(target.id) { vm.load(target) }
    LaunchedEffect(vm.toast) { vm.toast?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.toast = null } }
    LaunchedEffect(vm.blocked) { if (vm.blocked) onBack() }
    BackHandler(onBack = onBack)

    val user = vm.user ?: target
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    var confirmBlock by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    var pickingTypes by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = bottomPadding + 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading)
                }
                Box(Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreHoriz, "More", tint = c.heading) }
                    UserActionsMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        user = user,
                        isFriend = vm.isFriend,
                        context = context,
                        onAddToType = { vm.loadTypes(); pickingTypes = true },
                        onRemove = { confirmRemove = true },
                        onReport = { reporting = true },
                        onBlock = { confirmBlock = true },
                    )
                }
            }

            UserAvatar(user, 128.dp)
            Spacer(Modifier.height(16.dp))
            Text(user.displayName, style = onest(24.sp, FontWeight.Bold), color = c.text, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(Modifier.height(4.dp))
            Text(user.handle, style = onest(16.sp), color = c.textSecondary)
            user.bio?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = onest(14.sp), color = c.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
            }
            Spacer(Modifier.height(16.dp))
            RelationshipButtons(vm)

            vm.error?.let { Box(Modifier.padding(horizontal = 32.dp, vertical = 8.dp)) { ErrorBanner(it) { vm.error = null } } }

            Spacer(Modifier.height(28.dp))
            InterestsCard(vm.interests, vm.socials, showSocials = vm.isFriend, firstName = user.firstName, modifier = Modifier.padding(horizontal = 32.dp))

            Spacer(Modifier.height(28.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (vm.isFriend) {
                    Text("Activities by ${user.firstName}", style = onest(16.sp, FontWeight.SemiBold), color = c.textSecondary)
                    if (vm.activities.isEmpty()) {
                        Text("${user.firstName} doesn't have any upcoming activities.", style = onest(14.sp), color = c.placeholder)
                    }
                    vm.activities.forEach { a -> ActivityCard(a, Spawn.colors.indexFor(a.id), onClick = { onOpenActivity(a) }) }
                } else {
                    LockedActivities(user.firstName)
                }
            }
        }
    }

    if (confirmRemove) ConfirmDialog("Remove ${user.firstName} as a friend?", "You can always add them back later.", "Remove", { confirmRemove = false }) {
        confirmRemove = false; vm.removeFriend()
    }
    if (confirmBlock) ConfirmDialog("Block ${user.firstName}?", "They won't be able to see your activities or find you, and they'll be removed from your friends.", "Block", { confirmBlock = false }) {
        confirmBlock = false; vm.block("Blocked from profile")
    }
    if (reporting) ReportDialog("Report ${user.firstName}", onDismiss = { reporting = false }) { type, desc -> reporting = false; vm.report(type, desc) }

    if (vm.justBecameFriends) {
        ModalBottomSheet(onDismissRequest = { vm.justBecameFriends = false }, containerColor = c.surface) {
            Column(Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, null, tint = Palette.Green500, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Success!", style = onest(22.sp, FontWeight.SemiBold), color = c.heading)
                Spacer(Modifier.height(4.dp))
                Text("You've added ${user.displayName} as a friend", style = onest(15.sp), color = c.textSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Add to Activity Type", { vm.justBecameFriends = false; vm.loadTypes(); pickingTypes = true })
            }
        }
    }

    if (pickingTypes) AddToActivityTypeSheet(vm, user, onDismiss = { pickingTypes = false })
}

@Composable
private fun RelationshipButtons(vm: UserProfileViewModel) {
    when (vm.relationship) {
        UserRelationshipType.FRIEND ->
            ProfilePill("Friends", Icons.Default.HowToReg, bg = Palette.HappeningNow, fg = Color.Black.copy(alpha = 0.8f)) {}
        UserRelationshipType.OUTGOING_FRIEND_REQUEST ->
            ProfilePill("Request Sent", Icons.Default.Schedule, bg = Color.Transparent, fg = SpawnTheme.colors.text, border = true, onClick = vm::cancelRequest)
        UserRelationshipType.INCOMING_FRIEND_REQUEST -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfilePill("Accept Request", Icons.Default.HowToReg, bg = Palette.Indigo500, fg = Color.White, onClick = vm::accept)
            ProfilePill("Deny", null, bg = Color.Transparent, fg = SpawnTheme.colors.text, border = true, onClick = vm::deny)
        }
        else -> ProfilePill("Add as Friend", Icons.Default.PersonAdd, bg = Palette.Indigo500, fg = Color.White, onClick = vm::addFriend)
    }
}

@Composable
private fun ProfilePill(text: String, icon: ImageVector?, bg: Color, fg: Color, border: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .then(if (border) Modifier.border(1.dp, fg, RoundedCornerShape(100.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = onest(16.sp, FontWeight.Medium), color = fg)
    }
}

/** Red-outlined "Interests + Hobbies" card with tilted WhatsApp / Instagram badges (Figma profile). */
@Composable
fun InterestsCard(interests: List<String>, socials: UserSocialMediaDTO?, showSocials: Boolean, firstName: String, modifier: Modifier = Modifier) {
    val c = SpawnTheme.colors
    val context = LocalContext.current
    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (c.isDark) Color.White.copy(alpha = 0.1f) else Palette.Red500.copy(alpha = 0.06f))
                .border(1.dp, Palette.Red500, RoundedCornerShape(12.dp))
                .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 18.dp),
        ) {
            if (interests.isEmpty()) {
                Text("$firstName hasn't added any interests yet.", style = onest(13.sp), color = c.placeholder)
            } else {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    interests.forEachIndexed { i, s ->
                        Text(s, style = onest(12.sp, FontWeight.SemiBold), color = c.text, modifier = Modifier.offset(y = if (i % 2 == 0) 0.dp else 4.dp))
                    }
                }
            }
        }
        Box(
            Modifier.padding(start = 8.dp).clip(RoundedCornerShape(12.dp)).background(Palette.Red500).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                buildAnnotatedString { append("Interests "); withStyle(SpanStyle(color = Color.White)) { append("+") }; append(" Hobbies") },
                style = onest(14.sp, FontWeight.Bold), color = Palette.Gray900,
            )
        }
        if (showSocials && socials != null) {
            Row(Modifier.align(Alignment.TopEnd).padding(end = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                socials.whatsappLink?.takeIf { it.isNotBlank() }?.let { link ->
                    SocialBadge(R.drawable.social_whatsapp, -9f) { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) } }
                }
                socials.instagramLink?.takeIf { it.isNotBlank() }?.let { link ->
                    SocialBadge(R.drawable.social_instagram, 7f) { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) } }
                }
            }
        }
    }
}

@Composable
private fun SocialBadge(res: Int, angle: Float, onClick: () -> Unit) {
    Image(
        painterResource(res), null,
        modifier = Modifier
            .rotate(angle)
            .size(36.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    )
}

@Composable
private fun LockedActivities(firstName: String) {
    val c = SpawnTheme.colors
    val stroke = c.placeholder
    Column(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .drawBehind {
                drawRoundRect(
                    stroke, cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Lock, null, tint = c.placeholder, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text("Add $firstName to see their\nupcoming activities!", style = onest(14.sp, FontWeight.Medium), color = c.placeholder, textAlign = TextAlign.Center)
    }
}

/** Figma "Add Friend to Tag": pick which of your activity types this friend belongs to. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToActivityTypeSheet(vm: UserProfileViewModel, user: BaseUserDTO, onDismiss: () -> Unit) {
    val c = SpawnTheme.colors
    var selected by remember(vm.types) { mutableStateOf(vm.typesContainingUser()) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Add to Activity Type", style = onest(20.sp, FontWeight.SemiBold), color = c.heading)
            Spacer(Modifier.height(16.dp))
            UserAvatar(user, 56.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                if (selected.isEmpty()) "Where do you want to put ${user.firstName}?" else "Adding ${user.firstName} to ${selected.size} activity type${if (selected.size == 1) "" else "s"}",
                style = onest(15.sp, FontWeight.SemiBold), color = c.text,
            )
            Text("Tap to select / deselect", style = onest(12.sp), color = c.textSecondary)
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(GridCells.Fixed(3), Modifier.heightIn(max = 380.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.types, key = { it.id }) { t ->
                    val on = t.id in selected
                    Column(
                        Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (on) Palette.Indigo500.copy(alpha = 0.15f) else c.typeButton)
                            .border(if (on) 1.5.dp else 0.dp, if (on) Palette.Indigo500 else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { selected = if (on) selected - t.id else selected + t.id }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(t.icon, fontSize = 26.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(t.title, style = onest(13.sp, FontWeight.SemiBold), color = if (on) Palette.Indigo600 else c.heading, maxLines = 1)
                        Text("${t.associatedFriends.size + (if (on && t.associatedFriends.none { it.id == user.id }) 1 else 0) - (if (!on && t.associatedFriends.any { it.id == user.id }) 1 else 0)} people", style = onest(11.sp), color = c.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Save", { vm.saveTypes(selected, onDismiss) }, loading = vm.busy)
            Spacer(Modifier.height(16.dp))
        }
    }
}
