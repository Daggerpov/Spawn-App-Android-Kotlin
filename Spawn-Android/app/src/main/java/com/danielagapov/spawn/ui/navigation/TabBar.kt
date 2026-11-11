package com.danielagapov.spawn.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.R
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

enum class Tab(val route: String, val label: String, @DrawableRes val icon: Int, @DrawableRes val activeIcon: Int) {
    Home("home", "Home", R.drawable.ic_tab_home, R.drawable.ic_tab_home_active),
    Map("map", "Map", R.drawable.ic_tab_map, R.drawable.ic_tab_map_active),
    Activities("activities", "Activities", R.drawable.ic_tab_activities, R.drawable.ic_tab_activities_active),
    Friends("friends", "Friends", R.drawable.ic_tab_friends, R.drawable.ic_tab_friends_active),
    Profile("profile", "Profile", R.drawable.ic_tab_profile, R.drawable.ic_tab_profile_active),
}

val TabBarHeight = 72.dp

/** Floating pill tab bar from Figma: translucent lavender, active tab gets a white tile. */
@Composable
fun SpawnTabBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier, badges: Map<Tab, Int> = emptyMap()) {
    val c = SpawnTheme.colors
    Row(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(TabBarHeight)
            .shadow(16.dp, RoundedCornerShape(36.dp), spotColor = Color(0x403B4BC9), ambientColor = Color(0x203B4BC9))
            .clip(RoundedCornerShape(36.dp))
            .background(c.tabBar)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            val tint by animateColorAsState(if (active) c.tabActive else c.tabInactive, label = "tint")
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (active) (if (c.isDark) Color.White.copy(alpha = 0.12f) else Color.White) else Color.Transparent)
                    .clickable(remember { MutableInteractionSource() }, null) { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Icon(painterResource(if (active) tab.activeIcon else tab.icon), tab.label, tint = tint, modifier = Modifier.size(30.dp))
                        val count = badges[tab] ?: 0
                        if (count > 0) {
                            Box(
                                Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp).size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFD4E4C)),
                                contentAlignment = Alignment.Center,
                            ) { Text(if (count > 9) "9+" else "$count", style = onest(9.sp, FontWeight.Bold), color = Color.White) }
                        }
                    }
                    Text(tab.label, style = onest(11.sp, if (active) FontWeight.SemiBold else FontWeight.Medium), color = tint)
                }
            }
        }
    }
}
