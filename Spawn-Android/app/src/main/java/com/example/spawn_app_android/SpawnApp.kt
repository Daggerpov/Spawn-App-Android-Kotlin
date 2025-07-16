package com.example.spawn_app_android

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.*
import com.example.spawn_app_android.presentation.navigation.BottomNavItem
import com.example.spawn_app_android.presentation.screens.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SpawnApp() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Map,
        BottomNavItem.Activities,
        BottomNavItem.Friends,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentDestination == item.route,
                        onClick = {
                            if (currentDestination != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Map.route) { MapPage() }
            composable(BottomNavItem.Activities.route) { ActivitesScreen() }
            composable(BottomNavItem.Friends.route) { FriendsScreen() }
            composable(BottomNavItem.Profile.route) { ProfilePage() }
        }
    }
}
