package com.example.spawn_app_android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spawn_app_android.SpawnApp
import com.example.spawn_app_android.presentation.screens.authFlow.LoginPage


@Composable
fun AppRoot() {
    val navController = rememberNavController()
//    val authViewModel: AuthViewModel = viewModel()
//    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoggedIn = true

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginPage(
                onLoginSuccess = {
//                    authViewModel.setLoggedIn(true)
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            SpawnApp() // contains BottomNav + its own sub-NavHost
        }
    }
}
