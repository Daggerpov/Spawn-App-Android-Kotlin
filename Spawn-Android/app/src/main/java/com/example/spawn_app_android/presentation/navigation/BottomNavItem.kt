package com.example.spawn_app_android.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Map : BottomNavItem("map", "Map", Icons.Default.Place)
    object Activities : BottomNavItem("activities", "Activities", Icons.Default.Star)
    object Friends : BottomNavItem("friends", "Friends", Icons.Default.Person)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.AccountCircle)
}