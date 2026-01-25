package com.example.spawn_app_android.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spawn_app_android.SpawnApp
import com.example.spawn_app_android.presentation.screens.authFlow.AccountNotFoundScreen
import com.example.spawn_app_android.presentation.screens.authFlow.LoginPage
import com.example.spawn_app_android.presentation.screens.authFlow.UserInfoInputScreen
import com.example.spawn_app_android.presentation.viewModels.AuthState
import com.example.spawn_app_android.presentation.viewModels.AuthViewModel


@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    // Check for existing session on app launch
    LaunchedEffect(Unit) {
        authViewModel.checkExistingSession(context)
    }

    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthState.Initial -> "splash"
        is AuthState.Loading -> "splash"
        is AuthState.Authenticated -> "main"
        is AuthState.Unauthenticated, is AuthState.Error -> "login"
        is AuthState.NeedsOnboarding -> "login"
        is AuthState.UserNotFound -> "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            // Simple splash/loading screen while checking auth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            // Navigate when auth state is determined
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
        }

        composable("login") {
            LoginPage(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNeedsOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = false }
                    }
                },
                onUserNotFound = {
                    navController.navigate("accountNotFound") {
                        popUpTo("login") { inclusive = false }
                    }
                },
                authViewModel = authViewModel,
            )
        }

        composable("accountNotFound") {
            AccountNotFoundScreen(
                onRegisterNow = {
                    authViewModel.proceedToOnboarding()
                    navController.navigate("onboarding") {
                        popUpTo("accountNotFound") { inclusive = true }
                    }
                },
                onReturnToLogin = {
                    authViewModel.returnToLogin()
                    navController.navigate("login") {
                        popUpTo("accountNotFound") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            UserInfoInputScreen(
                authViewModel = authViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onComplete = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            SpawnApp(authViewModel)
        }
    }
}
