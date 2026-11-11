package com.danielagapov.spawn.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.danielagapov.spawn.R

sealed class BottomNavItem(val route: String, val label: String, val iconResId: Int) {
    object Home : BottomNavItem("home", "Home", R.drawable.home_line)
    object Map : BottomNavItem("map", "Map", R.drawable.marker_pin_04)
    object Activities : BottomNavItem("activities", "Activities", R.drawable.stars_01)
    object Friends : BottomNavItem("friends", "Friends", R.drawable.users_03)
    object Profile : BottomNavItem("profile", "Profile", R.drawable.user_circle)
}