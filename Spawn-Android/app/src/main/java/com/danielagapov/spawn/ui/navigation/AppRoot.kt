package com.danielagapov.spawn.ui.navigation

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.model.UserStatus
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.presentation.screens.FriendsPage
import com.danielagapov.spawn.presentation.screens.MapPage
import com.danielagapov.spawn.presentation.screens.ProfilePage
import com.danielagapov.spawn.presentation.screens.activities.ActivityRoutes
import com.danielagapov.spawn.presentation.screens.activities.ActivityViewModel
import com.danielagapov.spawn.presentation.screens.activities.createActivityNavGraph
import com.danielagapov.spawn.ui.activity.ActivityDetailSheet
import com.danielagapov.spawn.ui.auth.*
import com.danielagapov.spawn.ui.home.HomeScreen
import com.danielagapov.spawn.ui.home.HomeViewModel
import com.danielagapov.spawn.ui.theme.SpawnTheme

@Composable
fun AppRoot() {
    val auth by Spawn.auth.collectAsState()
    Box(Modifier.fillMaxSize().background(SpawnTheme.colors.background)) {
        when (val s = auth) {
            AuthState.Loading -> Unit // system splash stays up
            AuthState.SignedOut -> AuthFlow()
            is AuthState.Onboarding -> OnboardingFlow(s.status, needsPassword = s.user.provider == null || s.user.provider == "email")
            is AuthState.SignedIn -> MainScaffold(s.user)
        }
    }
}

@Composable
private fun AuthFlow() {
    val nav = rememberNavController()
    val vm: AuthViewModel = viewModel()
    NavHost(nav, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(onGetStarted = { nav.navigate("getin") }) }
        composable("getin") { GetInScreen(onCreateAccount = { vm.clearError(); nav.navigate("signup") }, onLogin = { vm.clearError(); nav.navigate("login") }) }
        composable("login") { LoginScreen(vm, onBack = { nav.popBackStack() }) }
        composable("signup") { SignupEmailScreen(vm, onBack = { nav.popBackStack() }, onCodeSent = { nav.navigate("verify") }) }
        composable("verify") { VerifyCodeScreen(vm, onBack = { nav.popBackStack() }) }
    }
}

/** Resumes onboarding at the step the backend says the account is on. */
@Composable
private fun OnboardingFlow(status: UserStatus?, needsPassword: Boolean) {
    val nav = rememberNavController()
    val vm: AuthViewModel = viewModel()
    val start = when (status) {
        UserStatus.EMAIL_VERIFIED, null -> "details"
        UserStatus.USERNAME_AND_PHONE_NUMBER -> "name"
        UserStatus.NAME_AND_PHOTO, UserStatus.CONTACT_IMPORT -> "terms"
        UserStatus.ACTIVE -> "terms"
    }
    NavHost(nav, startDestination = start) {
        composable("details") { AccountDetailsScreen(vm, needsPassword) { nav.navigate("name") } }
        composable("name") { NameScreen(vm) { nav.navigate("terms") } }
        composable("terms") { TermsScreen(vm) }
    }
}

@Composable
private fun MainScaffold(user: BaseUserDTO) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var openActivity by remember { mutableStateOf<FullFeedActivityDTO?>(null) }
    val homeVm: HomeViewModel = viewModel()

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        UserLocation.refresh(context)
    }
    LaunchedEffect(Unit) {
        if (UserLocation.hasPermission(context)) UserLocation.refresh(context)
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    val openProfile: (BaseUserDTO) -> Unit = { u ->
        if (u.id == user.id) { openActivity = null; tab = Tab.Profile }
        else Toast.makeText(context, "${u.displayName} (@${u.username})", Toast.LENGTH_SHORT).show()
    }

    Box(Modifier.fillMaxSize()) {
        val bottomPad = TabBarHeight + 24.dp
        when (tab) {
            Tab.Home -> HomeScreen(
                user = user, vm = homeVm,
                onOpenActivity = { openActivity = it },
                onQuickCreate = { tab = Tab.Activities },
                onSeeAllActivities = { tab = Tab.Map },
                bottomPadding = bottomPad,
            )
            Tab.Map -> Box(Modifier.fillMaxSize().padding(bottom = bottomPad)) { MapPage() }
            Tab.Activities -> {
                val nav = rememberNavController()
                val createVm: ActivityViewModel = viewModel()
                Box(Modifier.fillMaxSize().padding(bottom = bottomPad)) {
                    NavHost(nav, startDestination = ActivityRoutes.STEP1) { createActivityNavGraph(nav, createVm) }
                }
            }
            Tab.Friends -> Box(Modifier.fillMaxSize().padding(bottom = bottomPad).statusBarsPadding()) { FriendsPage() }
            Tab.Profile -> Box(Modifier.fillMaxSize().padding(bottom = bottomPad)) { ProfilePage() }
        }
        SpawnTabBar(tab, { tab = it }, Modifier.align(Alignment.BottomCenter))
    }

    openActivity?.let { a ->
        ActivityDetailSheet(a, onDismiss = { openActivity = null }, onOpenProfile = openProfile)
    }
}

