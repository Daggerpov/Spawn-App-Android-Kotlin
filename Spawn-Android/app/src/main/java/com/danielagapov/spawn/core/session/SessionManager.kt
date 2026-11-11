package com.danielagapov.spawn.core.session

import android.content.Context
import com.danielagapov.spawn.core.model.AuthResponseDTO
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.core.model.UserStatus
import com.danielagapov.spawn.core.network.NetworkModule
import com.danielagapov.spawn.core.network.SpawnApi
import com.danielagapov.spawn.core.network.TokenStore
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.util.ActivityColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    /** Splash: checking stored tokens with quick-sign-in. */
    data object Loading : AuthState
    data object SignedOut : AuthState
    /** Signed in but onboarding isn't finished (status != ACTIVE). */
    data class Onboarding(val user: BaseUserDTO, val status: UserStatus?) : AuthState
    data class SignedIn(val user: BaseUserDTO) : AuthState
}

/**
 * App-wide singletons (no DI framework — the app is small enough).
 * Mirrors the iOS UserAuthViewModel.shared + APIService pairing.
 */
object Spawn {
    lateinit var tokens: TokenStore
        private set
    lateinit var api: SpawnApi
        private set
    lateinit var colors: ActivityColors
        private set

    private val _auth = MutableStateFlow<AuthState>(AuthState.Loading)
    val auth: StateFlow<AuthState> = _auth.asStateFlow()

    /** Bumped whenever something the feed shows changes (activity created/joined/deleted). */
    private val _feedVersion = MutableStateFlow(0)
    val feedVersion: StateFlow<Int> = _feedVersion.asStateFlow()

    val currentUser: BaseUserDTO?
        get() = when (val s = _auth.value) {
            is AuthState.SignedIn -> s.user
            is AuthState.Onboarding -> s.user
            else -> null
        }

    val userId: String get() = currentUser?.id ?: ""

    fun init(context: Context) {
        tokens = TokenStore(context)
        colors = ActivityColors(context)
        api = NetworkModule.api(NetworkModule.okHttp(tokens) { signOutLocally() })
    }

    suspend fun restoreSession() {
        if (!tokens.hasTokens) {
            _auth.value = AuthState.SignedOut
            return
        }
        _auth.value = try {
            stateFor(api.quickSignIn().bodyOrThrow())
        } catch (e: Exception) {
            // Offline with tokens: stay signed out only if the server rejected us.
            if (e is com.danielagapov.spawn.core.network.ApiException) tokens.clear()
            AuthState.SignedOut
        }
    }

    fun onAuthenticated(response: AuthResponseDTO) {
        _auth.value = stateFor(response)
    }

    fun updateUser(user: BaseUserDTO) {
        _auth.value = when (val s = _auth.value) {
            is AuthState.SignedIn -> s.copy(user = user)
            is AuthState.Onboarding -> s.copy(user = user)
            else -> s
        }
    }

    fun completeOnboarding(user: BaseUserDTO) {
        _auth.value = AuthState.SignedIn(user)
    }

    fun feedChanged() {
        _feedVersion.value = _feedVersion.value + 1
    }

    fun signOutLocally() {
        tokens.clear()
        _auth.value = AuthState.SignedOut
    }

    private fun stateFor(r: AuthResponseDTO): AuthState =
        if (r.status == null || r.status == UserStatus.ACTIVE || r.user.hasCompletedOnboarding == true) {
            AuthState.SignedIn(r.user)
        } else {
            AuthState.Onboarding(r.user, r.status)
        }
}
