package com.example.spawn_app_android

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.spawn_app_android.presentation.navigation.BottomNavItem
import com.example.spawn_app_android.presentation.screens.*
import com.example.spawn_app_android.presentation.screens.activities.ActivityRoutes
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel
import com.example.spawn_app_android.presentation.screens.activities.createActivityNavGraph
import com.example.spawn_app_android.presentation.screens.authFlow.LoginPage
import com.example.spawn_app_android.presentation.viewModels.AuthViewModel

@Composable
fun SpawnApp(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Map,
        BottomNavItem.Activities,
        BottomNavItem.Friends,
        BottomNavItem.Profile
    )

    val authViewModel: AuthViewModel = viewModel()
    val activityViewModel: ActivityViewModel = viewModel()
    val isLoggedIn by authViewModel._isLoggedIn.collectAsState()


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
                        selected = navController
                            .currentBackStackEntryAsState()
                            .value
                            ?.destination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true,
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
            composable("login") {
                LoginPage(
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true } // removes login from backstack
                        }
                    },
                    authViewModel = authViewModel
                )
            }

            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Map.route) { MapPage() }

            navigation(
                startDestination = ActivityRoutes.STEP1,
                route = BottomNavItem.Activities.route
            ) {
                createActivityNavGraph(navController, activityViewModel)
            }

            composable(BottomNavItem.Friends.route) { FriendsPage() }
            composable(BottomNavItem.Profile.route) { ProfilePage() }
        }
    }
}
