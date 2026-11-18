package com.danielagapov.spawn.ui.profile

import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import com.danielagapov.spawn.ui.components.InAppBanners
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Brush
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.components.ActivityCard
import com.danielagapov.spawn.ui.components.LoadingBox
import java.time.YearMonth
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danielagapov.spawn.BuildConfig
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.ui.components.Avatar
import com.danielagapov.spawn.ui.components.EmptyState
import com.danielagapov.spawn.ui.components.PrimaryButton
import com.danielagapov.spawn.ui.components.SpawnTextField
import com.danielagapov.spawn.ui.components.ErrorBanner
import com.danielagapov.spawn.ui.components.UserAvatar
import com.danielagapov.spawn.ui.friends.ConfirmDialog
import com.danielagapov.spawn.ui.friends.SmallButton
import com.danielagapov.spawn.ui.friends.shareProfile
import com.danielagapov.spawn.ui.theme.ActivityPalette
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private enum class ProfilePage { Main, Edit, Settings, Account, ChangePassword, Blocked, Calendar, Day }

/** Profile tab (Figma "Your Profile", "Edit Profile", "Settings & Preferences"). */
@Composable
fun MyProfileTab(user: BaseUserDTO, onOpenActivity: (FullFeedActivityDTO) -> Unit, bottomPadding: Dp, vm: MyProfileViewModel = viewModel()) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf(ProfilePage.Main) }
    var dayBack by rememberSaveable { mutableStateOf(ProfilePage.Main) }
    LaunchedEffect(vm.toast) { vm.toast?.let { InAppBanners.success(it); vm.toast = null } }
    LaunchedEffect(vm.error) { vm.error?.let { InAppBanners.error(it); vm.error = null } }
    BackHandler(page != ProfilePage.Main) {
        page = when (page) {
            ProfilePage.Account, ProfilePage.Blocked -> ProfilePage.Settings
            ProfilePage.ChangePassword -> ProfilePage.Account
            ProfilePage.Day -> dayBack
            else -> ProfilePage.Main
        }
    }
    // A day with one activity opens it; a day with more opens the "Events - <day>" list (Figma 1557:3742).
    fun openDay(date: LocalDate, items: List<CalendarActivityDTO>, from: ProfilePage) {
        when {
            items.size == 1 -> vm.openCalendarActivity(items[0], onOpenActivity)
            items.size > 1 -> { vm.openDay(date, items); dayBack = from; page = ProfilePage.Day }
        }
    }

    AnimatedContent(page, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "profile") { p ->
        when (p) {
            ProfilePage.Main -> ProfileMain(
                user, vm, bottomPadding, onEdit = { page = ProfilePage.Edit }, onSettings = { page = ProfilePage.Settings },
                onDay = { date, items -> openDay(date, items, ProfilePage.Main) }, onFullCalendar = { page = ProfilePage.Calendar },
            )
            ProfilePage.Edit -> EditProfile(user, vm, bottomPadding, onClose = { page = ProfilePage.Main })
            ProfilePage.Settings -> SettingsPage(
                vm, bottomPadding, onBack = { page = ProfilePage.Main },
                onAccount = { page = ProfilePage.Account }, onBlocked = { vm.loadBlocked(); page = ProfilePage.Blocked },
            )
            ProfilePage.Account -> AccountSettingsPage(vm, bottomPadding, onBack = { page = ProfilePage.Settings }, onChangePassword = { page = ProfilePage.ChangePassword })
            ProfilePage.ChangePassword -> ChangePasswordPage(vm, bottomPadding, onBack = { page = ProfilePage.Account })
            ProfilePage.Blocked -> BlockedUsersPage(vm, bottomPadding, onBack = { page = ProfilePage.Settings })
            ProfilePage.Calendar -> CalendarPage(vm, bottomPadding, onBack = { page = ProfilePage.Main }, onDay = { date, items -> openDay(date, items, ProfilePage.Calendar) })
            ProfilePage.Day -> DayActivitiesPage(vm, bottomPadding, onBack = { page = dayBack }, onOpenActivity = onOpenActivity)
        }
    }
}

// region Main
@Composable
private fun ProfileMain(
    user: BaseUserDTO,
    vm: MyProfileViewModel,
    bottomPadding: Dp,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onDay: (LocalDate, List<CalendarActivityDTO>) -> Unit,
    onFullCalendar: () -> Unit,
) {
    val c = SpawnTheme.colors
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.load() }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(bottom = bottomPadding + 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Menu, "Settings", tint = c.heading, modifier = Modifier.size(28.dp))
            }
        }
        UserAvatar(user, 128.dp)
        Spacer(Modifier.height(16.dp))
        Text(user.displayName, style = onest(24.sp, FontWeight.Bold), color = c.text, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(user.handle, style = onest(16.sp), color = Palette.Gray400)
        user.bio?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = onest(14.sp), color = c.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineAction("Edit Profile", Icons.Outlined.Edit, onEdit)
            OutlineAction("Share Profile", Icons.Outlined.Share) { shareProfile(context, user) }
        }
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            Stat(Icons.Default.Link, vm.stats?.peopleMet, "People\nmet")
            Stat(Icons.Default.Star, vm.stats?.spawnsMade, "Spawns\nmade")
            Stat(Icons.Default.EditCalendar, vm.stats?.spawnsJoined, "Spawns\njoined")
        }
        Spacer(Modifier.height(28.dp))
        InterestsCard(vm.interests, vm.socials, showSocials = true, firstName = "You", modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(28.dp))
        ActivityCalendar(
            vm.month, vm.calendar, onMonthChange = vm::changeMonth, onDay = onDay, onOpenFull = onFullCalendar,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Composable
private fun OutlineAction(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(SpawnTheme.colors.surface)
            .border(1.25.dp, Palette.Indigo500, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Palette.Indigo500, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = onest(15.sp, FontWeight.SemiBold), color = Palette.Indigo500)
    }
}

@Composable
private fun Stat(icon: ImageVector, value: Int?, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Palette.Gray400, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(value?.toString() ?: "–", style = onest(20.sp, FontWeight.Bold), color = Palette.Gray400)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = onest(12.sp), color = Palette.Gray400, textAlign = TextAlign.Center)
    }
}

/**
 * Month grid from the Figma: gray squares, colored squares with the activity's emoji, dashed padding days.
 * Used on your own profile and a friend's (Figma 1760:3527); [onOpenFull] makes the month title open the full calendar.
 */
@Composable
internal fun ActivityCalendar(
    month: YearMonth,
    items: List<CalendarActivityDTO>,
    onMonthChange: (Long) -> Unit,
    onDay: (LocalDate, List<CalendarActivityDTO>) -> Unit,
    onOpenFull: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val c = SpawnTheme.colors
    val byDay = items.groupBy { runCatching { LocalDate.parse(it.date.take(10)) }.getOrNull() }
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value % 7 // Sunday-first like the Figma
    val cells = lead + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    val today = LocalDate.now()
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Tapping the month opens the full "Your Event Calendar" page (Figma 1321:11975, iOS ActivityCalendarView)
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .then(if (onOpenFull != null) Modifier.clickable(onClickLabel = "Open calendar", onClick = onOpenFull) else Modifier)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    style = onest(16.sp, FontWeight.SemiBold), color = c.textSecondary,
                )
                if (onOpenFull != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { onMonthChange(-1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", tint = c.textSecondary) }
            IconButton(onClick = { onMonthChange(1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month", tint = c.textSecondary) }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, style = onest(13.sp, FontWeight.Medium), color = Palette.Gray300, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        val gray = if (c.isDark) Palette.Gray700 else Color(0xFFDCD6D6)
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0 until 7) {
                    val idx = r * 7 + col
                    val day = idx - lead + 1
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (day < 1 || day > month.lengthOfMonth()) {
                            Box(
                                Modifier.fillMaxSize().alpha(0.3f).drawBehind {
                                    drawRoundRect(gray, cornerRadius = CornerRadius(6.dp.toPx()), style = Stroke(1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))))
                                },
                            )
                        } else {
                            val date = month.atDay(day)
                            val items = byDay[date].orEmpty()
                            val colorIndex = items.firstOrNull()?.let(::calendarColorIndex)
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .shadow(2.dp, RoundedCornerShape(6.dp))
                                    .clip(RoundedCornerShape(6.dp))
                                    .activityCellBackground(colorIndex, gray)
                                    .then(if (date == today) Modifier.border(2.dp, Palette.Indigo500, RoundedCornerShape(6.dp)) else Modifier)
                                    .clickable(enabled = items.isNotEmpty()) { onDay(date, items) },
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    items.size > 1 -> Text(items.take(2).joinToString("") { it.icon ?: "⭐️" }, fontSize = 14.sp)
                                    items.size == 1 -> Text(items[0].icon ?: "⭐️", fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
/** Same color assignment as the activity cards a day opens (the backend's colorHexCode is ignored, like iOS). */
private fun calendarColorIndex(item: CalendarActivityDTO): Int = Spawn.colors.indexFor(item.activityId ?: item.id)

/** Calendar cell fill: the activity card's Figma radial gradient, or plain gray for an empty day. */
private fun Modifier.activityCellBackground(colorIndex: Int?, empty: Color): Modifier =
    if (colorIndex == null) background(empty)
    else drawBehind {
        drawRect(ActivityPalette.card(colorIndex, size.width, size.height))
        // soft top highlight, matching ActivityCard
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent), endY = size.height * 0.35f))
    }

@Composable
internal fun SubPageHeader(title: String, onBack: () -> Unit) {
    val c = SpawnTheme.colors
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading) }
        Text(title, style = onest(20.sp, FontWeight.SemiBold), color = c.heading, modifier = Modifier.align(Alignment.Center))
    }
}

/** Figma "Your Event Calendar" (1321:11975 / 1321:12496): scrollable months of day tiles, four per row. */
@Composable
private fun CalendarPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit, onDay: (LocalDate, List<CalendarActivityDTO>) -> Unit) {
    LaunchedEffect(Unit) { vm.loadCalendarMonths() }
    val listState = rememberLazyListState()
    val months = vm.calendarMonths
    LaunchedEffect(months.isNotEmpty()) {
        val current = months.indexOfFirst { it.first == YearMonth.now() }
        if (current > 0) listState.scrollToItem(current)
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubPageHeader("Your Event Calendar", onBack)
        if (months.isEmpty()) LoadingBox()
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            items(months, key = { it.first.toString() }) { (month, items) -> CalendarMonthGrid(month, items, onDay) }
        }
    }
}

@Composable
private fun CalendarMonthGrid(month: YearMonth, items: List<CalendarActivityDTO>, onDay: (LocalDate, List<CalendarActivityDTO>) -> Unit) {
    val c = SpawnTheme.colors
    val byDay = remember(items) { items.groupBy { runCatching { LocalDate.parse(it.date.take(10)) }.getOrNull() } }
    val today = LocalDate.now()
    val gray = if (c.isDark) Palette.Gray700 else Color(0xFFE6E2E2)
    val days = month.lengthOfMonth()
    Column(Modifier.fillMaxWidth()) {
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = onest(16.sp, FontWeight.SemiBold), color = c.textSecondary,
        )
        Spacer(Modifier.height(12.dp))
        for (row in 0 until (days + 3) / 4) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0 until 4) {
                    val day = row * 4 + col + 1
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (day <= days) {
                            val date = month.atDay(day)
                            val dayItems = byDay[date].orEmpty()
                            val colorIndex = dayItems.firstOrNull()?.let(::calendarColorIndex)
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .activityCellBackground(colorIndex, gray)
                                    .then(if (date == today) Modifier.border(2.dp, Palette.Indigo500, RoundedCornerShape(10.dp)) else Modifier)
                                    .clickable(enabled = dayItems.isNotEmpty()) { onDay(date, dayItems) },
                            ) {
                                when {
                                    dayItems.size > 1 -> Text(dayItems.take(2).joinToString("") { it.icon ?: "⭐️" }, fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
                                    dayItems.size == 1 -> Text(dayItems[0].icon ?: "⭐️", fontSize = 36.sp, modifier = Modifier.align(Alignment.Center))
                                }
                                Text(
                                    "$day", style = onest(11.sp, FontWeight.Medium), color = if (colorIndex != null) Color.White else Palette.Gray500,
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 7.dp, bottom = 5.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** Figma "Events - May 16" (1557:3742, iOS DayActivitiesPageView): every activity on that day. */
@Composable
private fun DayActivitiesPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit, onOpenActivity: (FullFeedActivityDTO) -> Unit) {
    val title = vm.dayDate?.let { "Events - ${it.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${it.dayOfMonth}" } ?: "Events"
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubPageHeader(title, onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (vm.dayLoading) item { LoadingBox() }
            items(vm.dayActivities, key = { it.id }) { a ->
                ActivityCard(a, Spawn.colors.indexFor(a.id), onClick = { onOpenActivity(a) })
            }
        }
    }
}
// endregion

// region Edit profile
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfile(user: BaseUserDTO, vm: MyProfileViewModel, bottomPadding: Dp, onClose: () -> Unit) {
    val c = SpawnTheme.colors
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(user.name ?: "") }
    var username by rememberSaveable { mutableStateOf(user.username ?: "") }
    var bio by rememberSaveable { mutableStateOf(user.bio ?: "") }
    var interests by remember { mutableStateOf(vm.interests) }
    var newInterest by rememberSaveable { mutableStateOf("") }
    var instagram by rememberSaveable { mutableStateOf(vm.socials?.instagramHandle ?: "") }
    var whatsapp by rememberSaveable { mutableStateOf(vm.socials?.whatsappDisplay ?: "") }
    var photo by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { if (it != null) photo = it }

    val changed = name != (user.name ?: "") || username != (user.username ?: "") || bio != (user.bio ?: "") ||
        interests != vm.interests || instagram != (vm.socials?.instagramHandle ?: "") || whatsapp != (vm.socials?.whatsappDisplay ?: "") || photo != null
    fun addInterest() {
        val t = newInterest.trim()
        if (t.isNotEmpty() && interests.size < 7 && interests.none { it.equals(t, ignoreCase = true) }) interests = interests + t
        newInterest = ""
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().imePadding().padding(start = 24.dp, end = 24.dp, bottom = bottomPadding + 16.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Edit Profile", style = onest(24.sp, FontWeight.SemiBold), color = c.heading, modifier = Modifier.weight(1f))
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).border(1.dp, Palette.Red500, RoundedCornerShape(4.dp)).clickable(onClick = onClose).padding(horizontal = 16.dp, vertical = 10.dp),
            ) { Text("Cancel", style = onest(16.sp, FontWeight.Medium), color = Palette.Red500) }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (changed) Palette.Indigo500 else Palette.Indigo100)
                    .clickable(enabled = changed && !vm.saving) {
                        if (newInterest.isNotBlank()) addInterest()
                        vm.save(context, name, username, bio, interests, instagram, whatsapp, photo, onClose)
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                if (vm.saving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                else Text("Save", style = onest(16.sp, FontWeight.Medium), color = if (changed) Color.White else Palette.Indigo300)
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.size(128.dp).clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
            if (photo != null) AsyncImage(photo, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
            else UserAvatar(user, 128.dp)
            Box(
                Modifier.align(Alignment.BottomEnd).size(40.dp).clip(CircleShape).background(Palette.Indigo500),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Edit, "Change photo", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        FieldLabel("Name")
        OutlinedInput(name, { name = it }, "Your name")
        FieldLabel("Username")
        OutlinedInput(username, { username = it.replace(" ", "").lowercase() }, "username", prefix = "@")
        FieldLabel("Bio")
        OutlinedInput(bio, { bio = it.take(150) }, "Tell people what you're into", singleLine = false)
        FieldLabel("Interests + Hobbies (Max 7)")
        OutlinedInput(
            newInterest, { newInterest = it.take(30) },
            if (interests.size >= 7) "You've added 7 interests" else "Type and press enter to add...",
            imeAction = ImeAction.Done, onDone = { addInterest() },
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            interests.forEach { tag ->
                Row(
                    Modifier.clip(RoundedCornerShape(100.dp)).background(if (c.isDark) Palette.Gray700 else Color(0xFFDCD6D6)).padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(tag, style = onest(14.sp, FontWeight.Medium), color = c.text)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Close, "Remove $tag", tint = Palette.Red500, modifier = Modifier.size(16.dp).clickable { interests = interests - tag })
                }
            }
        }
        FieldLabel("Third party apps")
        SocialField(com.danielagapov.spawn.R.drawable.social_instagram, instagram, { instagram = it.trim().removePrefix("@") }, "Instagram username", Color(0xFFD62976), KeyboardType.Text, prefix = "@")
        Spacer(Modifier.height(10.dp))
        SocialField(com.danielagapov.spawn.R.drawable.social_whatsapp, whatsapp, { whatsapp = it }, "WhatsApp number", Palette.Green500, KeyboardType.Phone, transformation = PhoneSpacing)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Spacer(Modifier.height(18.dp))
    Text(text, style = onest(16.sp), color = Palette.Gray400)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OutlinedInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    prefix: String? = null,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Default,
    onDone: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val c = SpawnTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).border(1.dp, Palette.Gray400, RoundedCornerShape(6.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (prefix != null) { Text(prefix, style = onest(16.sp), color = Palette.Gray300); Spacer(Modifier.width(4.dp)) }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = onest(16.sp), color = c.placeholder)
            BasicTextField(
                value, onChange,
                singleLine = singleLine,
                textStyle = onest(16.sp).copy(color = c.text),
                cursorBrush = SolidColor(Palette.Indigo500),
                keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
                keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SocialField(
    icon: Int,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    accent: Color,
    keyboardType: KeyboardType,
    prefix: String? = null,
    transformation: VisualTransformation = VisualTransformation.None,
) {
    val c = SpawnTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).border(1.dp, accent, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(icon), null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(12.dp))
        if (prefix != null && value.isNotEmpty()) Text(prefix, style = onest(16.sp, FontWeight.SemiBold), color = c.text)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = onest(16.sp), color = c.placeholder)
            BasicTextField(
                value, onChange, singleLine = true,
                textStyle = onest(16.sp, FontWeight.SemiBold).copy(color = c.text),
                cursorBrush = SolidColor(Palette.Indigo500),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = transformation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
// endregion

// region Settings
@Composable
private fun SettingsPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit, onAccount: () -> Unit, onBlocked: () -> Unit) {
    val c = SpawnTheme.colors
    val context = LocalContext.current
    var feedbackOpen by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    fun open(url: String) = runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading) }
            Text("Settings & Preferences", style = onest(20.sp, FontWeight.SemiBold), color = c.heading, modifier = Modifier.align(Alignment.Center))
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = bottomPadding + 16.dp)) {
            SettingsSection("Account Settings")
            SettingsRow(Icons.Outlined.LockReset, "Account", onClick = onAccount)
            SettingsSection("Privacy & Safety")
            SettingsRow(Icons.Outlined.Block, "Blocked users", onClick = onBlocked)
            SettingsSection("Contact Us")
            SettingsRow(Icons.Outlined.Feedback, "Send us feedback") { feedbackOpen = true }
            SettingsRow(Icons.Outlined.ChatBubbleOutline, "Contact us") { open("mailto:spawnappmarketing@gmail.com?subject=Spawn%20Android") }
            SettingsRow(Icons.Outlined.StarRate, "Rate Spawn") {
                open("market://details?id=${context.packageName}").onFailure { open("https://play.google.com/store/apps/details?id=${context.packageName}") }
            }
            SettingsSection("Socials")
            SettingsRow(Icons.Outlined.CameraAlt, "Instagram", external = true) { open("https://instagram.com/spawnapp") }
            SettingsSection("Legal")
            SettingsRow(Icons.Outlined.Policy, "Privacy Policy", external = true) { open(BuildConfig.PRIVACY_POLICY_URL) }
            SettingsSection("Authentication")
            ActionRow("Log Out", Icons.AutoMirrored.Filled.Logout, c.text) { confirmLogout = true }
            Spacer(Modifier.height(24.dp))
            Text("Spawn for Android ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = onest(12.sp), color = c.placeholder, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
    if (feedbackOpen) FeedbackDialog(onDismiss = { feedbackOpen = false }) { type, msg -> vm.sendFeedback(type, msg) { feedbackOpen = false } }
    if (confirmLogout) ConfirmDialog("Log out?", "You'll need to sign in again to see your activities.", "Log Out", { confirmLogout = false }) { confirmLogout = false; vm.logOut() }
}

/** Figma "Account Settings" (455:6297): Change Password for email accounts, then Delete Account. */
@Composable
private fun AccountSettingsPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit, onChangePassword: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubPageHeader("Account Settings", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = bottomPadding + 16.dp)) {
            // Google accounts have no Spawn password to change.
            if (!Spawn.isOAuthUser) {
                SettingsSection("Authentication")
                SettingsRow(Icons.Outlined.LockReset, "Change Password", onClick = onChangePassword)
            }
            SettingsSection("Permanent Actions")
            ActionRow("Delete Account", Icons.Outlined.Delete, Palette.Red600) { confirmDelete = true }
        }
    }
    if (confirmDelete) ConfirmDialog(
        "Delete your account?",
        "This permanently deletes your profile, activities, friends and messages. It can't be undone.",
        "Delete", { confirmDelete = false },
    ) { confirmDelete = false; vm.deleteAccount() }
}

/** iOS ChangePasswordView: current password, then the new one twice. */
@Composable
private fun ChangePasswordPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit) {
    // Plain remember on purpose: passwords stay out of saved instance state.
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var currentError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    fun submit() {
        newError = if (newPassword.length < 8) "Passwords need at least 8 characters." else null
        confirmError = if (newError == null && confirm != newPassword) "Passwords don't match." else null
        if (newError != null || confirmError != null) return
        vm.changePassword(current, newPassword, onWrongCurrent = { currentError = "Current password is incorrect." }, onDone = onBack)
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubPageHeader("Change Password", onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SpawnTextField(current, { current = it; currentError = null }, "Enter your current password", label = "Current Password", isPassword = true, error = currentError)
            SpawnTextField(newPassword, { newPassword = it; newError = null }, "Enter a strong password", label = "New Password", isPassword = true, error = newError)
            SpawnTextField(confirm, { confirm = it; confirmError = null }, "Re-enter password", label = "Confirm New Password", isPassword = true, error = confirmError)
            PrimaryButton(
                "Update Password", ::submit,
                enabled = current.isNotEmpty() && newPassword.isNotEmpty() && confirm.isNotEmpty(),
                loading = vm.changingPassword,
            )
        }
    }
}

/** A bold row with its icon on the right (Figma "Log Out", "Delete Account"). */
@Composable
private fun ActionRow(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = onest(17.sp, FontWeight.SemiBold), color = color, modifier = Modifier.weight(1f))
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsSection(title: String) {
    Spacer(Modifier.height(20.dp))
    Text(title, style = onest(14.sp, FontWeight.Medium), color = Palette.Gray400)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, danger: Boolean = false, external: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) Palette.Red600 else SpawnTheme.colors.text
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = onest(17.sp, FontWeight.Medium), color = color, modifier = Modifier.weight(1f))
        Icon(if (external) Icons.AutoMirrored.Filled.OpenInNew else Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedbackDialog(onDismiss: () -> Unit, onSend: (FeedbackType, String) -> Unit) {
    var type by remember { mutableStateOf(FeedbackType.GENERAL_FEEDBACK) }
    var message by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send us feedback", style = onest(18.sp, FontWeight.SemiBold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(FeedbackType.BUG to "🐞 Bug", FeedbackType.FEATURE_REQUEST to "💡 Idea", FeedbackType.GENERAL_FEEDBACK to "💬 General").forEach { (t, l) ->
                        FilterChip(type == t, { type = t }, label = { Text(l) })
                    }
                }
                OutlinedTextField(message, { message = it }, placeholder = { Text("What's on your mind?") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton({ onSend(type, message) }, enabled = message.isNotBlank()) { Text("Send") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun BlockedUsersPage(vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit) {
    val c = SpawnTheme.colors
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.heading) }
            Text("Blocked Users", style = onest(20.sp, FontWeight.SemiBold), color = c.heading, modifier = Modifier.align(Alignment.Center))
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (vm.blocked.isEmpty()) item { EmptyState("🙌", "No blocked users", "People you block won't be able to see you or your activities.") }
            items(vm.blocked, key = { it.id ?: it.blockedId ?: "" }) { b ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(b.blockedProfilePicture, b.blockedName ?: b.blockedUsername, 36.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(b.blockedName ?: b.blockedUsername ?: "User", style = onest(14.sp, FontWeight.SemiBold), color = c.text)
                        b.blockedUsername?.let { Text("@$it", style = onest(12.sp), color = c.textSecondary) }
                    }
                    SmallButton("Unblock", { vm.unblock(b) })
                }
            }
        }
    }
}
// endregion

/** Shows North American numbers with spaces ("+1 604 555 1001", "604 555 1001") without changing the stored value. */
private object PhoneSpacing : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val s = text.text
        val withCountry = Regex("^\\+1\\d{10}$").matches(s)
        val local = Regex("^\\d{10}$").matches(s)
        if (!withCountry && !local) return TransformedText(text, OffsetMapping.Identity)
        val cuts = if (withCountry) listOf(2, 5, 8) else listOf(3, 6)
        val out = buildString { s.forEachIndexed { i, ch -> if (i in cuts) append(' '); append(ch) } }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset + cuts.count { it < offset }
            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                cuts.forEachIndexed { i, cut -> if (offset > cut + i) o-- }
                return o.coerceIn(0, s.length)
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}
