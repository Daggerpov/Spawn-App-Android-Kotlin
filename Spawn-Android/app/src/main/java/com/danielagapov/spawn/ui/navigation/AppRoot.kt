package com.danielagapov.spawn.ui.navigation

import android.Manifest
import android.widget.Toast
import kotlinx.coroutines.flow.filterNotNull
import androidx.activity.compose.BackHandler
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
import com.danielagapov.spawn.core.session.DeepLink
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.ui.map.MapTab
import com.danielagapov.spawn.ui.create.CreateActivityFlow
import com.danielagapov.spawn.ui.create.CreateActivityViewModel
import com.danielagapov.spawn.ui.activity.ActivityDetailSheet
import com.danielagapov.spawn.ui.auth.*
import com.danielagapov.spawn.ui.home.HomeScreen
import com.danielagapov.spawn.ui.home.HomeViewModel
import com.danielagapov.spawn.ui.friends.FriendsTab
import com.danielagapov.spawn.ui.friends.FriendsViewModel
import com.danielagapov.spawn.ui.profile.MyProfileTab
import com.danielagapov.spawn.ui.profile.UserProfileScreen
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
    val createVm: CreateActivityViewModel = viewModel()
    val friendsVm: FriendsViewModel = viewModel()
    var profileTarget by remember { mutableStateOf<BaseUserDTO?>(null) }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        UserLocation.refresh(context)
    }
    LaunchedEffect(Unit) {
        if (UserLocation.hasPermission(context)) UserLocation.refresh(context)
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    val openProfile: (BaseUserDTO) -> Unit = { u ->
        openActivity = null
        if (u.id == user.id) { profileTarget = null; tab = Tab.Profile }
        else profileTarget = u
    }

    // Open getspawn.com links (activity drawer or profile), resolving share codes like iOS does
    LaunchedEffect(Unit) {
        Spawn.pendingLink.filterNotNull().collect { l ->
        Spawn.consumeLink()
        runCatching {
            when (l) {
                is DeepLink.Activity -> {
                    val id = if (DeepLink.isUuid(l.key)) l.key else Spawn.api.resolveActivityShareCode(l.key).bodyOrThrow().id
                    profileTarget = null
                    openActivity = Spawn.api.activity(id, user.id, autoJoin = true).bodyOrThrow()
                    Spawn.feedChanged()
                }
                is DeepLink.Profile -> {
                    val id = if (DeepLink.isUuid(l.key)) l.key else Spawn.api.resolveProfileShareCode(l.key).bodyOrThrow().id
                    if (id == user.id) { profileTarget = null; tab = Tab.Profile }
                    else { tab = Tab.Friends; profileTarget = Spawn.api.getUser(id, user.id).bodyOrThrow() }
                }
            }
        }.onFailure { Toast.makeText(context, "Couldn't open that link: ${it.friendlyMessage()}", Toast.LENGTH_LONG).show() }
        }
    }

    // Back on another tab returns Home before leaving the app (pages inside tabs register their own handlers later, so they win).
    BackHandler(enabled = tab != Tab.Home && profileTarget == null) { tab = Tab.Home }

    Box(Modifier.fillMaxSize()) {
        val bottomPad = TabBarHeight + 24.dp
        when (tab) {
            Tab.Home -> HomeScreen(
                user = user, vm = homeVm,
                onOpenActivity = { openActivity = it },
                onQuickCreate = { type ->
                    createVm.reset()
                    if (type != null) createVm.pickType(type)
                    tab = Tab.Activities
                },
                onSeeAllActivities = { tab = Tab.Map },
                bottomPadding = bottomPad,
            )
            Tab.Map -> MapTab(homeVm.activities, onOpenActivity = { openActivity = it }, bottomPadding = bottomPad)
            Tab.Activities -> CreateActivityFlow(createVm, onDone = { tab = Tab.Home }, bottomPadding = bottomPad)
            Tab.Friends -> FriendsTab(friendsVm, onOpenProfile = openProfile, bottomPadding = bottomPad)
            Tab.Profile -> MyProfileTab(user, onOpenActivity = { openActivity = it }, bottomPadding = bottomPad)
        }
        profileTarget?.let { p ->
            UserProfileScreen(p, onBack = { profileTarget = null; friendsVm.load() }, onOpenActivity = { openActivity = it }, bottomPadding = bottomPad)
        }
        SpawnTabBar(
            tab, { profileTarget = null; tab = it }, Modifier.align(Alignment.BottomCenter),
            badges = mapOf(Tab.Friends to friendsVm.incoming.size).filterValues { it > 0 },
        )
    }

    openActivity?.let { a ->
        ActivityDetailSheet(a, onDismiss = { openActivity = null }, onOpenProfile = openProfile)
    }
}

