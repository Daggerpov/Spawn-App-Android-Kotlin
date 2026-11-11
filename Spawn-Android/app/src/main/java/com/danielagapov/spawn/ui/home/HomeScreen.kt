package com.danielagapov.spawn.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.core.model.ActivityTypeDTO
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.components.*
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: BaseUserDTO,
    vm: HomeViewModel,
    onOpenActivity: (FullFeedActivityDTO) -> Unit,
    onQuickCreate: (ActivityTypeDTO?) -> Unit,
    onSeeAllActivities: () -> Unit,
    bottomPadding: Dp = 0.dp,
) {
    LaunchedEffect(user.id) { if (vm.activities.isEmpty()) vm.load() }
    @Suppress("UNUSED_EXPRESSION") vm.clock // recompose on the 30s tick
    val c = SpawnTheme.colors

    Box(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .background(
                // Figma's faint bottom-center glow behind the feed
                Brush.radialGradient(
                    listOf(Palette.Indigo300.copy(alpha = if (c.isDark) 0.10f else 0.22f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(540f, 2400f), radius = 1400f,
                )
            ),
    ) {
        PullToRefreshBox(isRefreshing = vm.refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 28.dp, bottom = 14.dp)) {
                        UserAvatar(user, 52.dp)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "Hey ${user.firstName}! 👋", style = onest(32.sp, FontWeight.Bold), color = c.heading,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                item { SectionHeader("Spawn in!", onClick = { onQuickCreate(null) }) }
                item {
                    ActivityTypeRow(vm.activityTypes.take(4), onPick = { onQuickCreate(it) })
                }
                item { Spacer(Modifier.height(4.dp)); SectionHeader("See what’s happening", onClick = onSeeAllActivities) }
                when {
                    vm.loading && vm.activities.isEmpty() -> item { LoadingBox() }
                    vm.error != null && vm.activities.isEmpty() -> item { ErrorBanner(vm.error!!) { vm.load() } }
                    vm.activities.isEmpty() -> item {
                        EmptyState(
                            "🌙", "No Activities Found",
                            "We couldn't find any activities nearby.\nStart one yourself and be spontaneous!",
                        ) { PrimaryButton("Create an Activity", { onQuickCreate(null) }, modifier = Modifier.width(240.dp)) }
                    }
                    else -> items(vm.activities, key = { it.id }) { a ->
                        ActivityCard(a, Spawn.colors.indexFor(a.id), onClick = { onOpenActivity(a) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

/** The four "Spawn in!" quick-create buttons (Figma: cool gray tiles, emoji + navy label, hold for more). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityTypeRow(types: List<ActivityTypeDTO>, onPick: (ActivityTypeDTO) -> Unit) {
    val c = SpawnTheme.colors
    val haptics = LocalHapticFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        if (types.isEmpty()) {
            repeat(4) {
                Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).background(c.typeButton))
            }
        }
        types.forEach { t ->
            Column(
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.typeButton)
                    .combinedClickable(
                        onClick = { onPick(t) },
                        onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onPick(t) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(t.icon, fontSize = 34.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    t.title, style = onest(15.sp, FontWeight.Medium), color = if (c.isDark) Color.White else Palette.FigmaNavy,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

