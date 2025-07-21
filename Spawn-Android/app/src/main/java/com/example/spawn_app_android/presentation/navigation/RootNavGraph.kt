package com.example.spawn_app_android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spawn_app_android.SpawnApp
import com.example.spawn_app_android.presentation.screens.authFlow.LoginPage
import com.example.spawn_app_android.presentation.viewModels.AuthViewModel


@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
//    val isLoggedIn by authViewModel._isLoggedIn.collectAsState()
//    val isLoggedIn = false

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.getLoggedIn()) "main" else "login"
    ) {
        composable("login") {
            LoginPage(
                onLoginSuccess = {
                    authViewModel.setLoggedIn(true)
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("main") {
            SpawnApp(authViewModel) // contains BottomNav + its own sub-NavHost
        }
    }
}
