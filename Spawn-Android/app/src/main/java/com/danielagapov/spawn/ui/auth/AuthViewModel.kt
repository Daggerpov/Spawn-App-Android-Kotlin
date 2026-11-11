package com.danielagapov.spawn.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.BuildConfig
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.ApiException
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Auth + onboarding state machine (iOS UserAuthViewModel, trimmed to what Android needs):
 *  - email/username + password login
 *  - Google sign-in (Credential Manager) -> /auth/sign-in, or /auth/register/oauth for new users
 *  - email sign-up: send code -> verify -> username/password -> name -> accept terms
 */
class AuthViewModel : ViewModel() {
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    var signupEmail by mutableStateOf("")
        private set
    var resendSeconds by mutableStateOf(0)
        private set

    private val api get() = Spawn.api

    fun clearError() { error = null }

    private fun launch(block: suspend () -> Unit) {
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            try { block() } catch (e: Exception) {
                Log.w("Auth", "auth call failed", e)
                error = e.friendlyMessage()
            } finally { loading = false }
        }
    }

    fun login(usernameOrEmail: String, password: String) = launch {
        try {
            val r = api.login(LoginDTO(usernameOrEmail.trim(), password)).bodyOrThrow()
            Spawn.onAuthenticated(r)
        } catch (e: ApiException) {
            error = when (e.code) {
                401, 404 -> "That username/email and password don't match."
                else -> e.message
            }
        }
    }

    fun googleSignIn(context: Context, isSignup: Boolean) {
        if (BuildConfig.WEB_CLIENT_ID.isBlank()) {
            error = "Google sign-in isn't set up in this build yet. Use email for now."
            return
        }
        launch {
            val option = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val result = try {
                CredentialManager.create(context).getCredential(context, request)
            } catch (_: GetCredentialCancellationException) {
                return@launch
            }
            val cred = result.credential
            if (cred !is CustomCredential || cred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                error = "Couldn't read your Google account."
                return@launch
            }
            val google = GoogleIdTokenCredential.createFrom(cred.data)
            val email = google.id
            try {
                val r = api.signIn(google.idToken, email, "google").bodyOrThrow()
                Spawn.onAuthenticated(r)
            } catch (e: ApiException) {
                if (e.code != 404) throw e
                // New to Spawn: create the account from the Google profile, then onboard.
                val r = api.registerOAuth(
                    OAuthRegistrationDTO(
                        idToken = google.idToken, provider = "google", email = email,
                        name = google.displayName, profilePictureUrl = google.profilePictureUri?.toString(),
                    )
                ).bodyOrThrow()
                Spawn.onAuthenticated(r)
            }
        }
    }

    fun sendCode(email: String, onSent: () -> Unit) = launch {
        val clean = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
            error = "Enter a valid email address."
            return@launch
        }
        val r = api.sendVerification(EmailVerificationSendDTO(clean)).bodyOrThrow()
        signupEmail = clean
        resendSeconds = (r.secondsUntilNextAttempt ?: 30L).toInt().coerceAtMost(120)
        onSent()
        tickResend()
    }

    private fun tickResend() {
        viewModelScope.launch {
            while (resendSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                resendSeconds -= 1
            }
        }
    }

    fun resend() = sendCode(signupEmail) {}

    fun verifyCode(code: String) = launch {
        try {
            val r = api.checkVerification(EmailVerificationVerifyDTO(signupEmail, code)).bodyOrThrow()
            Spawn.onAuthenticated(r)
        } catch (e: ApiException) {
            error = if (e.code == 400 || e.code == 401) "That code isn't right. Check your email and try again." else e.message
        }
    }

    fun submitAccountDetails(username: String, password: String?, phone: String?, onDone: () -> Unit) = launch {
        val user = Spawn.currentUser ?: return@launch
        val u = username.trim().removePrefix("@")
        if (!Regex("^[a-zA-Z0-9._]{3,30}$").matches(u)) {
            error = "Usernames are 3–30 letters, numbers, dots or underscores."
            return@launch
        }
        if (password != null && password.length < 8) {
            error = "Passwords need at least 8 characters."
            return@launch
        }
        val updated = api.updateUserDetails(
            UpdateUserDetailsDTO(id = user.id, username = u, phoneNumber = phone?.takeIf { it.isNotBlank() } ?: "", password = password)
        ).bodyOrThrow()
        Spawn.updateUser(updated)
        onDone()
    }

    fun submitName(name: String, onDone: () -> Unit) = launch {
        val user = Spawn.currentUser ?: return@launch
        if (name.isBlank()) {
            error = "What should your friends call you?"
            return@launch
        }
        val updated = api.setOptionalDetails(user.id, OptionalDetailsDTO(name.trim())).bodyOrThrow()
        Spawn.updateUser(updated)
        onDone()
    }

    fun acceptTerms() = launch {
        val user = Spawn.currentUser ?: return@launch
        // Android has no contact import step yet; mark it done so the account becomes ACTIVE.
        runCatching { api.completeContactImport(user.id).bodyOrThrow() }
        val updated = api.acceptTos(user.id).bodyOrThrow()
        // Refresh tokens/role now that the account is active.
        val r = runCatching { api.quickSignIn().bodyOrThrow() }.getOrNull()
        Spawn.completeOnboarding(r?.user ?: updated)
    }
}
