package com.example.spawn_app_android.presentation.viewModels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spawn_app_android.data.remote.*
import com.example.spawn_app_android.data.remote.dto.*
import com.example.spawn_app_android.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * AuthViewModel.kt
 *
 * Created by Ethan Dsouza on 2025-12-06
 *
 * Handles all authentication flows including:
 * - Google Sign-In
 * - Email/Password Sign-In
 * - OAuth Registration
 * - User state management
 * - Onboarding flow navigation
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val PREFS_NAME = "spawn_auth_state"

        // SharedPreferences keys
        private const val KEY_HAS_LAUNCHED_BEFORE = "hasLaunchedBefore"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        private const val KEY_HAS_SEEN_PREVIEW_SCREENS = "hasSeenPreviewScreens"

        // Google OAuth Client ID from local.properties -> BuildConfig
        private val GOOGLE_WEB_CLIENT_ID: String = BuildConfig.WEB_CLIENT_ID
    }

    // ==================== DEPENDENCIES ====================

    private val repository = ApiRepository()
    private val tokenManager: TokenManager? = ApiClient.getTokenManager()
    private val prefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ==================== NAVIGATION STATE ====================

    sealed class NavigationState {
        object None : NavigationState()
        object SpawnIntro : NavigationState()
        object SignIn : NavigationState()
        object VerificationCode : NavigationState()
        data class UserDetailsInput(val isOAuthUser: Boolean) : NavigationState()
        object UserOptionalDetailsInput : NavigationState()
        object ContactImport : NavigationState()
        object UserTermsOfService : NavigationState()
        object OnboardingContinuation : NavigationState()
        object FeedView : NavigationState()
    }

    enum class UserStatus(val value: String) {
        EMAIL_VERIFIED("EMAIL_VERIFIED"),
        USERNAME_AND_PHONE_NUMBER("USERNAME_AND_PHONE_NUMBER"),
        NAME_AND_PHOTO("NAME_AND_PHOTO"),
        CONTACT_IMPORT("CONTACT_IMPORT"),
        ACTIVE("ACTIVE");

        companion object {
            fun fromValue(value: String): UserStatus? {
                return entries.find { it.value == value }
            }
        }
    }

    /**
     * Auth provider type
     */
    enum class AuthProviderType(val value: String) {
        GOOGLE("google"),
        APPLE("apple"),
        EMAIL("email")
    }

    /**
     * Delete account alert types
     */
    sealed class DeleteAccountAlertType {
        object DeleteConfirmation : DeleteAccountAlertType()
        object DeleteSuccess : DeleteAccountAlertType()
        object DeleteError : DeleteAccountAlertType()
    }

    /**
     * Auth alert types for authentication-related errors
     */
    sealed class AuthAlertType {
        object CreateError : AuthAlertType()
        object NetworkError : AuthAlertType()
        object UsernameAlreadyInUse : AuthAlertType()
        object EmailAlreadyInUse : AuthAlertType()
        object PhoneNumberAlreadyInUse : AuthAlertType()
        object ProviderMismatch : AuthAlertType()
        object ProviderUnavailable : AuthAlertType()
        object EmailVerificationFailed : AuthAlertType()
        object InvalidToken : AuthAlertType()
        object TokenExpired : AuthAlertType()
        object AccountFoundSigningIn : AuthAlertType()
        data class UnknownError(val message: String) : AuthAlertType()
    }

    // ==================== STATE FLOWS ====================

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _hasCheckedSpawnUserExistence = MutableStateFlow(false)
    val hasCheckedSpawnUserExistence: StateFlow<Boolean> = _hasCheckedSpawnUserExistence.asStateFlow()

    private val _spawnUser = MutableStateFlow<BaseUserDTO?>(null)
    val spawnUser: StateFlow<BaseUserDTO?> = _spawnUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _authProvider = MutableStateFlow<AuthProviderType?>(null)
    val authProvider: StateFlow<AuthProviderType?> = _authProvider.asStateFlow()

    private val _externalUserId = MutableStateFlow<String?>(null)
    val externalUserId: StateFlow<String?> = _externalUserId.asStateFlow()

    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email.asStateFlow()

    private val _profilePicUrl = MutableStateFlow<String?>(null)
    val profilePicUrl: StateFlow<String?> = _profilePicUrl.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.None)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeAlert = MutableStateFlow<DeleteAccountAlertType?>(null)
    val activeAlert: StateFlow<DeleteAccountAlertType?> = _activeAlert.asStateFlow()

    private val _authAlert = MutableStateFlow<AuthAlertType?>(null)
    val authAlert: StateFlow<AuthAlertType?> = _authAlert.asStateFlow()

    private val _isAutoSigningIn = MutableStateFlow(false)
    val isAutoSigningIn: StateFlow<Boolean> = _isAutoSigningIn.asStateFlow()

    private val _defaultPfpFetchError = MutableStateFlow(false)
    val defaultPfpFetchError: StateFlow<Boolean> = _defaultPfpFetchError.asStateFlow()

    private val _defaultPfpUrlString = MutableStateFlow<String?>(null)
    val defaultPfpUrlString: StateFlow<String?> = _defaultPfpUrlString.asStateFlow()

    private val _secondsUntilNextVerificationAttempt = MutableStateFlow(30)
    val secondsUntilNextVerificationAttempt: StateFlow<Int> = _secondsUntilNextVerificationAttempt.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _hasSeenPreviewScreens = MutableStateFlow(false)
    val hasSeenPreviewScreens: StateFlow<Boolean> = _hasSeenPreviewScreens.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    // ==================== PRIVATE STATE ====================

    // OAuth credential storage for account completion
    private var storedOAuthProvider: AuthProviderType? = null
    private var storedIdToken: String? = null
    private var storedEmail: String? = null

    // Flags for concurrent operation prevention
    private var isReauthenticating = false
    private var isNavigating = false
    private var isOnboarding = false

    // Minimum loading time tracking
    private var minimumLoadingCompleted = false
    private var authCheckCompleted = false

    // For continuing user onboarding
    private var continuingUserStatus: UserStatus? = null
    private var isOAuthUser = false

    // ==================== INITIALIZATION ====================

    init {
        initializeAuthState()
    }

    private fun initializeAuthState() {
        // Determine if this is truly a first launch
        val hasLaunchedBefore = prefs.getBoolean(KEY_HAS_LAUNCHED_BEFORE, false)
        if (!hasLaunchedBefore) {
            _isFirstLaunch.value = true
            prefs.edit().putBoolean(KEY_HAS_LAUNCHED_BEFORE, true).apply()
        } else {
            _isFirstLaunch.value = false
        }

        // Load onboarding completion status
        _hasCompletedOnboarding.value = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)

        // Load preview screens status
        _hasSeenPreviewScreens.value = prefs.getBoolean(KEY_HAS_SEEN_PREVIEW_SCREENS, false)

        // Start minimum loading timer and attempt quick sign-in
        viewModelScope.launch {
            // Minimum loading time (2 seconds)
            kotlinx.coroutines.delay(2000)
            minimumLoadingCompleted = true
            checkLoadingCompletion()
        }

        viewModelScope.launch {
            quickSignIn()
            authCheckCompleted = true
            checkLoadingCompletion()
        }
    }

    private fun checkLoadingCompletion() {
        if (minimumLoadingCompleted && authCheckCompleted) {
            _hasCheckedSpawnUserExistence.value = true
            Log.d(TAG, "🔄 DEBUG: Setting hasCheckedSpawnUserExistence to true")
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Clears authentication tokens
     */
    private fun clearTokens() {
        tokenManager?.clearTokens()
        Log.d(TAG, "ℹ️ Tokens cleared from storage")
    }

    /**
     * Clears all error states
     */
    private fun clearErrorStates() {
        _errorMessage.value = null
        _authAlert.value = null
        _isAutoSigningIn.value = false
    }

    // ==================== NAVIGATION HELPER METHODS ====================

    /**
     * Safely navigate to a new state with proper debouncing and protection
     */
    fun navigateTo(state: NavigationState, delayMs: Long = 100) {
        if (isNavigating) return

        viewModelScope.launch {
            isNavigating = true

            if (delayMs > 0) {
                kotlinx.coroutines.delay(delayMs)
            }

            _navigationState.value = state

            // Reset the navigation lock after a short delay
            kotlinx.coroutines.delay(500)
            isNavigating = false
        }
    }

    // ==================== STATE MANAGEMENT ====================

    /**
     * Resets all authentication state
     */
    fun resetState() {
        Log.d(TAG, "🔄 DEBUG: Resetting authentication state")

        // Preserve OAuth credentials during onboarding logout
        val wasOnboarding = !_hasCompletedOnboarding.value && _spawnUser.value != null
        if (wasOnboarding) {
            Log.d(TAG, "🔄 Preserving OAuth credentials during onboarding reset")
            storedOAuthProvider = _authProvider.value
            storedIdToken = _idToken.value
            storedEmail = _email.value
        }

        // Clear tokens
        clearTokens()

        // Reset user state
        _errorMessage.value = null
        _authProvider.value = null
        _externalUserId.value = null
        _idToken.value = null
        _isLoggedIn.value = false
        _spawnUser.value = null

        _name.value = null
        _email.value = null
        _profilePicUrl.value = null

        _isFormValid.value = false
        _navigationState.value = NavigationState.None

        _secondsUntilNextVerificationAttempt.value = 30
        _activeAlert.value = null

        _defaultPfpFetchError.value = false
        _defaultPfpUrlString.value = null

        // Restore OAuth credentials if this was an onboarding reset
        if (wasOnboarding) {
            Log.d(TAG, "🔄 Restoring OAuth credentials after onboarding reset")
            _authProvider.value = storedOAuthProvider
            _idToken.value = storedIdToken
            _email.value = storedEmail
        }

        // Reset loading state but mark as not first launch
        _hasCheckedSpawnUserExistence.value = true
        _isFirstLaunch.value = false
    }

    /**
     * Clear all error states - use this when navigating between auth screens
     */
    fun clearAllErrors() {
        clearErrorStates()
    }

    /**
     * Reset authentication flow state when navigating back during onboarding
     */
    fun resetAuthFlow() {
        Log.d(TAG, "🔄 DEBUG: Resetting auth flow state for back navigation")

        _spawnUser.value?.let { user ->
            Log.d(TAG, "🔄 DEBUG: Clearing incomplete user state - ID: ${user.id}, Email: ${user.email}")
        }

        clearErrorStates()

        _authProvider.value = null
        _externalUserId.value = null
        _idToken.value = null
        _isLoggedIn.value = false
        _spawnUser.value = null

        _name.value = null
        _email.value = null
        _profilePicUrl.value = null

        _isFormValid.value = false
        _navigationState.value = NavigationState.None

        _secondsUntilNextVerificationAttempt.value = 30
        _activeAlert.value = null

        _defaultPfpFetchError.value = false
        _defaultPfpUrlString.value = null

        isNavigating = false

        clearTokens()
    }

    /**
     * Mark onboarding as completed
     */
    fun markOnboardingCompleted() {
        _hasCompletedOnboarding.value = true
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true).apply()
    }

    /**
     * Reset onboarding state for testing/debugging purposes
     */
    fun resetOnboardingState() {
        _hasCompletedOnboarding.value = false
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, false).apply()
    }

    /**
     * Reset launch state for testing/debugging purposes
     */
    fun resetLaunchState() {
        _isFirstLaunch.value = true
        prefs.edit().putBoolean(KEY_HAS_LAUNCHED_BEFORE, false).apply()
        Log.d(TAG, "🔄 DEBUG: Reset launch state - will show loading screen on next restart")
    }

    // ==================== GOOGLE SIGN-IN ====================

    /**
     * Initiates Google Sign-In flow for login
     */
    suspend fun loginWithGoogle(context: Context) {
        clearStaleTokensIfNeeded()
        isOnboarding = false
        signInWithGoogle(context)
    }

    /**
     * Initiates Google Sign-In flow for registration
     */
    suspend fun googleRegister(context: Context) {
        isOnboarding = true
        signInWithGoogle(context)
    }

    /**
     * Clear stale tokens that might interfere with OAuth authentication
     */
    private suspend fun clearStaleTokensIfNeeded() {
        if (_isLoggedIn.value || _spawnUser.value != null) return

        val hasAccessToken = tokenManager?.getAccessToken() != null
        val hasRefreshToken = tokenManager?.getRefreshToken() != null

        if (hasAccessToken || hasRefreshToken) {
            Log.d(TAG, "🔄 DEBUG: Clearing potentially stale cached tokens before OAuth attempt")
            tokenManager?.clearTokens()

            _isLoggedIn.value = false
            _spawnUser.value = null
            _errorMessage.value = null
        }
    }

    /**
     * Performs Google Sign-In using Credential Manager
     */
    private suspend fun signInWithGoogle(context: Context) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            val googleIdToken = googleIdTokenCredential.idToken
            val googleId = googleIdTokenCredential.id
            val displayName = googleIdTokenCredential.displayName
            val profilePictureUri = googleIdTokenCredential.profilePictureUri

            _profilePicUrl.value = profilePictureUri?.toString()
            _name.value = displayName
            _email.value = googleId
            _isLoggedIn.value = true
            _externalUserId.value = googleId
            _authProvider.value = AuthProviderType.GOOGLE
            _idToken.value = googleIdToken

            if (isOnboarding) {
                registerWithOAuth(
                    idToken = googleIdToken,
                    provider = AuthProviderType.GOOGLE,
                    email = googleId,
                    name = displayName,
                    profilePictureUrl = profilePictureUri?.toString()
                )
            } else {
                spawnFetchUserIfAlreadyExists()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
            _errorMessage.value = "Google Sign-In failed: ${e.localizedMessage}"
        }
    }

    // ==================== SIGN OUT ====================

    /**
     * Signs out the current user
     */
    fun signOut() {
        // Clear tokens
        clearTokens()

        // Clear stored OAuth credentials for complete logout
        storedOAuthProvider = null
        storedIdToken = null
        storedEmail = null
        Log.d(TAG, "🔄 Cleared stored OAuth credentials for complete logout")

        resetState()
    }

    // ==================== SPAWN USER METHODS ====================

    /**
     * Checks if user exists in Spawn database after OAuth sign-in
     */
    suspend fun spawnFetchUserIfAlreadyExists() {
        val unwrappedIdToken = _idToken.value
        if (unwrappedIdToken == null) {
            _errorMessage.value = "ID Token is missing."
            Log.e(TAG, "ID Token is missing.")
            return
        }

        val unwrappedProvider = _authProvider.value
        if (unwrappedProvider == null) {
            Log.e(TAG, "Auth provider is missing.")
            return
        }

        val emailToUse = _email.value ?: ""

        when (val result = repository.loginWithOAuth(emailToUse, unwrappedIdToken)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                _isLoggedIn.value = true

                // Navigate based on user status
                // Note: In full implementation, parse status from response
                navigateTo(NavigationState.FeedView)
            }
            is ApiResult.Error -> {
                _spawnUser.value = null
                handleApiError(result.error)
            }
        }

        _hasCheckedSpawnUserExistence.value = true
    }

    /**
     * Handle API errors consistently
     */
    private fun handleApiError(error: ApiError) {
        when (error) {
            is ApiError.NotFound -> {
                _authAlert.value = AuthAlertType.UnknownError(
                    "We couldn't find your account. Please check your credentials or create a new account."
                )
                _errorMessage.value = "We couldn't find your account. Please check your credentials or create a new account."
            }
            is ApiError.Unauthorized -> {
                _authAlert.value = AuthAlertType.UnknownError(
                    "Authentication failed. Please try again."
                )
                _errorMessage.value = "Authentication failed. Please try again."
            }
            is ApiError.NetworkError -> {
                _authAlert.value = AuthAlertType.NetworkError
                _errorMessage.value = "Network connection error. Please check your internet connection and try again."
            }
            else -> {
                _authAlert.value = AuthAlertType.UnknownError(error.message)
                _errorMessage.value = error.message
            }
        }
    }

    /**
     * Creates a new user in Spawn
     */
    suspend fun spawnMakeUser(
        username: String,
        profilePicture: Bitmap?,
        name: String,
        email: String
    ) {
        _navigationState.value = NavigationState.None
        _isFormValid.value = false

        val userDTO = UserCreateDTO(
            username = username,
            name = name,
            email = email
        )

        var profilePicUrl: String? = null
        if (profilePicture == null && _profilePicUrl.value?.isNotEmpty() == true) {
            profilePicUrl = _profilePicUrl.value
        }

        when (val result = repository.createUser(userDTO, profilePicUrl)) {
            is ApiResult.Success -> {
                Log.d(TAG, "User created successfully: ${result.data.username}")
                _spawnUser.value = result.data
            }
            is ApiResult.Error -> {
                handleAccountCreationError(result.error)
            }
        }
    }

    /**
     * Quick sign-in using stored tokens
     */
    suspend fun quickSignIn() {
        Log.d(TAG, "🔄 DEBUG: quickSignIn() called - attempting quick sign-in")

        val storedEmail = tokenManager?.getUserEmail()
        if (storedEmail == null) {
            Log.d(TAG, "🔄 DEBUG: quickSignIn() - No stored email, re-login required")
            _isLoggedIn.value = false
            _spawnUser.value = null
            return
        }

        when (val result = repository.quickSignIn(storedEmail)) {
            is ApiResult.Success -> {
                Log.d(TAG, "🔄 DEBUG: quickSignIn() - Quick sign-in successful")
                _spawnUser.value = result.data
                _isLoggedIn.value = true

                if (!_hasCompletedOnboarding.value) {
                    markOnboardingCompleted()
                }

                navigateTo(NavigationState.FeedView)
            }
            is ApiResult.Error -> {
                Log.d(TAG, "🔄 DEBUG: quickSignIn() - Error performing quick-login. Re-login is required")
                _isLoggedIn.value = false
                _spawnUser.value = null
            }
        }
    }

    /**
     * Sign in with email/username and password
     */
    suspend fun signInWithEmailOrUsername(usernameOrEmail: String, password: String) {
        Log.d(TAG, "Attempting email/username sign-in")

        when (val result = repository.signIn(usernameOrEmail, password)) {
            is ApiResult.Success -> {
                Log.d(TAG, "Email/username login successful")
                _spawnUser.value = result.data
                _isLoggedIn.value = true
                _errorMessage.value = null

                // Save email for quick sign-in
                tokenManager?.saveUserEmail(usernameOrEmail)

                navigateTo(NavigationState.FeedView)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Failed to login with email/username: ${result.error}")
                _isLoggedIn.value = false
                _spawnUser.value = null

                when (result.error) {
                    is ApiError.Unauthorized -> {
                        _errorMessage.value = "Invalid email/username or password. Please check your credentials and try again."
                    }
                    is ApiError.NotFound -> {
                        _errorMessage.value = "Account not found. Please check your email/username or create a new account."
                    }
                    is ApiError.NetworkError -> {
                        _errorMessage.value = "Network connection error. Please check your internet connection and try again."
                    }
                    else -> {
                        _errorMessage.value = "Unable to sign in at this time. Please try again later."
                    }
                }
            }
        }
    }

    // ==================== EMAIL VERIFICATION ====================

    /**
     * Sends email verification code
     */
    suspend fun sendEmailVerification(email: String) {
        Log.d(TAG, "🔄 DEBUG: sendEmailVerification called with email: $email")

        when (val result = repository.sendVerificationCode(email)) {
            is ApiResult.Success -> {
                _navigationState.value = NavigationState.VerificationCode
                _email.value = email
                _authProvider.value = AuthProviderType.EMAIL
                _errorMessage.value = null
                Log.d(TAG, "📍 DEBUG: Set navigationState to .verificationCode")
            }
            is ApiResult.Error -> {
                Log.e(TAG, "❌ DEBUG: Email verification error: ${result.error}")
                when (result.error) {
                    is ApiError.ValidationError -> {
                        _errorMessage.value = "Please enter a valid email address."
                    }
                    is ApiError.HttpError -> {
                        if ((result.error as ApiError.HttpError).statusCode == 409) {
                            _errorMessage.value = "This email is already registered. Please try signing in instead."
                        } else {
                            _errorMessage.value = "Unable to send verification email. Please try again."
                        }
                    }
                    is ApiError.NetworkError -> {
                        _errorMessage.value = "Network connection error. Please check your internet connection and try again."
                    }
                    else -> {
                        _errorMessage.value = "Unable to send verification email. Please try again."
                    }
                }
            }
        }
    }

    /**
     * Verifies email verification code
     */
    suspend fun verifyEmailCode(email: String, code: String) {
        when (val result = repository.checkVerificationCode(email, code)) {
            is ApiResult.Success -> {
                _email.value = email
                _authProvider.value = AuthProviderType.EMAIL
                _errorMessage.value = null

                // Navigate to user details input
                navigateTo(NavigationState.UserDetailsInput(isOAuthUser = false))
            }
            is ApiResult.Error -> {
                when (result.error) {
                    is ApiError.ValidationError -> {
                        _errorMessage.value = "Invalid verification code"
                    }
                    is ApiError.NotFound -> {
                        _errorMessage.value = "Verification code not found"
                    }
                    else -> {
                        _errorMessage.value = "Failed to verify code"
                    }
                }
            }
        }
    }

    // ==================== OAUTH REGISTRATION ====================

    /**
     * Registers a new user via OAuth
     */
    suspend fun registerWithOAuth(
        idToken: String,
        provider: AuthProviderType,
        email: String?,
        name: String?,
        profilePictureUrl: String?
    ) {
        // Store OAuth credentials
        _authProvider.value = provider
        _idToken.value = idToken
        _email.value = email
        storedOAuthProvider = provider
        storedIdToken = idToken
        storedEmail = email
        Log.d(TAG, "🔐 Stored OAuth credentials for onboarding (provider: ${provider.value})")

        when (val result = repository.registerWithOAuth(
            email = email ?: "",
            idToken = idToken,
            name = name,
            profilePictureUrl = profilePictureUrl
        )) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                _authProvider.value = provider
                _idToken.value = idToken
                _email.value = email
                _isLoggedIn.value = true
                _errorMessage.value = null

                // Navigate based on user status
                navigateTo(NavigationState.UserDetailsInput(isOAuthUser = true))
            }
            is ApiResult.Error -> {
                when (result.error) {
                    is ApiError.HttpError -> {
                        val statusCode = (result.error as ApiError.HttpError).statusCode
                        when (statusCode) {
                            409 -> {
                                // User already exists - attempt to sign them in
                                Log.d(TAG, "📍 User already exists (409), attempting OAuth sign-in")
                                _isAutoSigningIn.value = true
                                _authAlert.value = AuthAlertType.AccountFoundSigningIn
                                signInWithOAuth(idToken, provider, email)
                            }
                            500 -> {
                                // Server error - try sign-in as fallback
                                Log.d(TAG, "📍 Server error (500), attempting OAuth sign-in as fallback")
                                _isAutoSigningIn.value = true
                                _authAlert.value = AuthAlertType.AccountFoundSigningIn
                                signInWithOAuth(idToken, provider, email)
                            }
                            else -> {
                                _authAlert.value = AuthAlertType.CreateError
                                _errorMessage.value = "Unable to create account. Please try again."
                            }
                        }
                    }
                    is ApiError.NetworkError -> {
                        _authAlert.value = AuthAlertType.NetworkError
                        _errorMessage.value = "Network connection error. Please check your internet connection and try again."
                    }
                    else -> {
                        _authAlert.value = AuthAlertType.UnknownError(result.error.message)
                        _errorMessage.value = result.error.message
                    }
                }
            }
        }
    }

    /**
     * OAuth sign-in for existing users
     */
    private suspend fun signInWithOAuth(idToken: String, provider: AuthProviderType, email: String?) {
        _authProvider.value = provider
        _idToken.value = idToken
        _email.value = email
        Log.d(TAG, "🔐 Setting OAuth credentials for re-authentication")

        when (val result = repository.loginWithOAuth(email ?: "", idToken)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                _email.value = result.data.email
                _errorMessage.value = null
                _isAutoSigningIn.value = false
                _authAlert.value = null
                _isLoggedIn.value = true

                // Navigate to appropriate screen
                navigateTo(NavigationState.FeedView)
                Log.d(TAG, "📍 OAuth re-authentication successful")
            }
            is ApiResult.Error -> {
                Log.e(TAG, "❌ OAuth re-authentication failed: ${result.error.message}")
                _authAlert.value = AuthAlertType.UnknownError(result.error.message)
                _errorMessage.value = result.error.message
                _isAutoSigningIn.value = false
            }
        }
    }

    // ==================== USER DETAILS UPDATE ====================

    /**
     * Updates user details (username, phone number, password)
     */
    suspend fun updateUserDetails(id: String, username: String, phoneNumber: String, password: String?) {
        if (!validateStoredTokens()) {
            Log.d(TAG, "🔄 No valid tokens found before updateUserDetails. Attempting OAuth re-authentication...")
            handleAuthenticationFailure()
            return
        }

        val userUpdate = UserUpdateDTO(
            username = username,
            phoneNumber = phoneNumber
        )

        when (val result = repository.updateUser(id, userUpdate)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                navigateTo(NavigationState.UserOptionalDetailsInput)
                _errorMessage.value = null
            }
            is ApiResult.Error -> {
                when (result.error) {
                    is ApiError.Unauthorized -> {
                        Log.d(TAG, "🔄 Authentication failed during user details update. Attempting re-authentication...")
                        handleAuthenticationFailure()
                    }
                    else -> {
                        _errorMessage.value = result.error.message
                    }
                }
            }
        }
    }

    /**
     * Updates optional user details (name, profile picture)
     */
    suspend fun updateOptionalDetails(id: String, name: String, profileImage: Bitmap?) {
        val optionalDetails = OptionalDetailsDTO(bio = name)

        when (val result = repository.updateOptionalDetails(id, optionalDetails)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                navigateTo(NavigationState.ContactImport)
                _errorMessage.value = null
            }
            is ApiResult.Error -> {
                _errorMessage.value = result.error.message
            }
        }
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    /**
     * Deletes the user's account
     */
    suspend fun deleteAccount() {
        val userId = _spawnUser.value?.id
        if (userId == null) {
            _activeAlert.value = DeleteAccountAlertType.DeleteError
            return
        }

        when (val result = repository.deleteUser(userId)) {
            is ApiResult.Success -> {
                clearTokens()
                _activeAlert.value = DeleteAccountAlertType.DeleteSuccess
            }
            is ApiResult.Error -> {
                when (result.error) {
                    is ApiError.Unauthorized -> {
                        Log.d(TAG, "Account deletion failed due to authentication error - clearing local data")
                        clearLocalDataAndLogout()
                        _activeAlert.value = DeleteAccountAlertType.DeleteSuccess
                    }
                    else -> {
                        _activeAlert.value = DeleteAccountAlertType.DeleteError
                    }
                }
            }
        }
    }

    private fun clearLocalDataAndLogout() {
        clearTokens()
        _spawnUser.value = null
        _isLoggedIn.value = false
        _hasCompletedOnboarding.value = false
    }

    /**
     * Changes user password
     */
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val userId = _spawnUser.value?.id
            ?: throw IllegalStateException("User ID not found")

        // Note: Implement actual password change API call
        Log.d(TAG, "Password change requested for user $userId")
    }

    /**
     * Fetches latest user data from backend
     */
    suspend fun fetchUserData() {
        val userId = _spawnUser.value?.id
        if (userId == null) {
            Log.d(TAG, "Cannot fetch user data: No user ID found")
            return
        }

        when (val result = repository.getUser(userId)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                Log.d(TAG, "User data refreshed: ${result.data.username}, ${result.data.name}")
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error fetching user data: ${result.error.message}")
            }
        }
    }

    /**
     * Updates profile picture
     */
    suspend fun updateProfilePicture(image: Bitmap) {
        val userId = _spawnUser.value?.id
        if (userId == null) {
            Log.d(TAG, "Cannot update profile picture: No user ID found")
            return
        }

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        val imageData = stream.toByteArray()

        Log.d(TAG, "Starting profile picture update for user $userId with image data size: ${imageData.size} bytes")

        when (val result = repository.updateProfilePicture(userId, imageData)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                Log.d(TAG, "Profile picture updated successfully")
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error updating profile picture: ${result.error.message}")
            }
        }
    }

    /**
     * Edits user profile (username and name)
     */
    suspend fun spawnEditProfile(username: String, name: String) {
        val userId = _spawnUser.value?.id
        if (userId == null) {
            Log.d(TAG, "Cannot edit profile: No user ID found")
            return
        }

        val updateDTO = UserUpdateDTO(
            username = username,
            name = name
        )

        when (val result = repository.updateUser(userId, updateDTO)) {
            is ApiResult.Success -> {
                _spawnUser.value = result.data
                Log.d(TAG, "Profile updated successfully: ${result.data.username}")
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error updating profile: ${result.error.message}")
            }
        }
    }

    // ==================== ONBOARDING METHODS ====================

    /**
     * Completes contact import step
     */
    suspend fun completeContactImport() {
        // Note: Implement contact import completion API
        navigateTo(NavigationState.UserTermsOfService)
        Log.d(TAG, "Successfully completed contact import, navigating to terms of service")
    }

    /**
     * Accepts terms of service
     */
    suspend fun acceptTermsOfService() {
        navigateTo(NavigationState.FeedView)
        _isLoggedIn.value = true
        _errorMessage.value = null
        markOnboardingCompleted()
        Log.d(TAG, "Successfully accepted Terms of Service, navigating to feed view")
    }

    /**
     * Navigates based on user status
     */
    fun navigateOnStatus() {
        val status = continuingUserStatus ?: return

        when (status) {
            UserStatus.EMAIL_VERIFIED -> {
                navigateTo(NavigationState.UserDetailsInput(isOAuthUser = isOAuthUser))
                Log.d(TAG, "📍 [AUTH] User status: emailVerified - navigating to user details input (OAuth: $isOAuthUser)")
            }
            UserStatus.USERNAME_AND_PHONE_NUMBER -> {
                navigateTo(NavigationState.UserOptionalDetailsInput)
                Log.d(TAG, "📍 [AUTH] User status: usernameAndPhoneNumber - navigating to optional details input")
            }
            UserStatus.NAME_AND_PHOTO -> {
                navigateTo(NavigationState.ContactImport)
                Log.d(TAG, "📍 [AUTH] User status: nameAndPhoto - navigating to contact import")
            }
            UserStatus.CONTACT_IMPORT -> {
                navigateTo(NavigationState.UserTermsOfService)
                Log.d(TAG, "📍 [AUTH] User status: contactImport - navigating to terms of service")
            }
            UserStatus.ACTIVE -> {
                _isFormValid.value = true
                navigateTo(NavigationState.FeedView)
                if (!_hasCompletedOnboarding.value) {
                    markOnboardingCompleted()
                }
                Log.d(TAG, "📍 [AUTH] User status: active - navigating to feed")
            }
        }
    }

    /**
     * Gets started - shows appropriate first screen
     */
    fun getStarted() {
        if (!_hasSeenPreviewScreens.value) {
            navigateTo(NavigationState.SpawnIntro)
        } else {
            navigateTo(NavigationState.SignIn)
        }
    }

    /**
     * Marks preview screens as seen
     */
    fun markPreviewScreensAsSeen() {
        _hasSeenPreviewScreens.value = true
        prefs.edit().putBoolean(KEY_HAS_SEEN_PREVIEW_SCREENS, true).apply()
        Log.d(TAG, "🔄 DEBUG: Marked preview screens as seen")
    }

    /**
     * Resets preview screens state for testing
     */
    fun resetPreviewScreensState() {
        _hasSeenPreviewScreens.value = false
        prefs.edit().putBoolean(KEY_HAS_SEEN_PREVIEW_SCREENS, false).apply()
        Log.d(TAG, "🔄 DEBUG: Reset preview screens state - will show preview screens on next Get Started")
    }

    // ==================== TOKEN VALIDATION ====================

    private fun validateStoredTokens(): Boolean {
        val hasAccessToken = tokenManager?.getAccessToken() != null
        val hasRefreshToken = tokenManager?.getRefreshToken() != null
        Log.d(TAG, "🔐 Token validation - Access: ${if (hasAccessToken) "✅" else "❌"}, Refresh: ${if (hasRefreshToken) "✅" else "❌"}")
        return hasAccessToken || hasRefreshToken
    }

    private fun handleAuthenticationFailure() {
        if (isReauthenticating) {
            Log.d(TAG, "🔄 Re-authentication already in progress, skipping duplicate attempt")
            return
        }

        isReauthenticating = true
        Log.d(TAG, "🔄 Starting authentication failure recovery...")

        var idToken = _idToken.value
        var authProvider = _authProvider.value
        var email = _email.value

        if (idToken == null || authProvider == null) {
            Log.d(TAG, "🔄 Current OAuth credentials not available, trying stored credentials...")
            idToken = storedIdToken
            authProvider = storedOAuthProvider
            email = storedEmail
        }

        if (idToken == null || authProvider == null || email == null) {
            Log.d(TAG, "🔄 No OAuth credentials available for re-authentication. Showing error instead of logging out.")
            isReauthenticating = false
            _authAlert.value = AuthAlertType.UnknownError("We're having trouble with your session. Please try again.")
            _errorMessage.value = "We're having trouble with your session. Please try again."
            return
        }

        Log.d(TAG, "🔄 Re-authenticating with OAuth credentials (provider: ${authProvider.value})...")

        tokenManager?.clearTokens()

        viewModelScope.launch {
            signInWithOAuth(idToken, authProvider, email)
            isReauthenticating = false
            Log.d(TAG, "🔄 Re-authentication attempt completed")
        }
    }

    // ==================== ERROR HANDLING ====================

    private fun handleAccountCreationError(error: ApiError) {
        when (error) {
            is ApiError.HttpError -> {
                when (error.statusCode) {
                    409 -> _authAlert.value = parseConflictError(error.message)
                    400 -> _authAlert.value = parseEmailVerificationError(error.message)
                    401 -> _authAlert.value = parseTokenError(error.message)
                    503 -> _authAlert.value = AuthAlertType.ProviderUnavailable
                    in 500..599 -> _authAlert.value = AuthAlertType.NetworkError
                    else -> _authAlert.value = AuthAlertType.UnknownError(error.message)
                }
            }
            else -> _authAlert.value = AuthAlertType.NetworkError
        }
    }

    private fun parseConflictError(errorMessage: String?): AuthAlertType {
        val message = errorMessage?.lowercase() ?: return AuthAlertType.CreateError

        return when {
            message.contains("username") || message.contains("duplicate") -> AuthAlertType.UsernameAlreadyInUse
            message.contains("email") -> AuthAlertType.EmailAlreadyInUse
            message.contains("phone") -> AuthAlertType.PhoneNumberAlreadyInUse
            message.contains("provider") -> AuthAlertType.ProviderMismatch
            else -> AuthAlertType.CreateError
        }
    }

    private fun parseEmailVerificationError(errorMessage: String?): AuthAlertType {
        val message = errorMessage?.lowercase() ?: return AuthAlertType.CreateError

        return if (message.contains("verification") || message.contains("code")) {
            AuthAlertType.EmailVerificationFailed
        } else {
            AuthAlertType.CreateError
        }
    }

    private fun parseTokenError(errorMessage: String?): AuthAlertType {
        val message = errorMessage?.lowercase() ?: return AuthAlertType.InvalidToken

        return if (message.contains("expired") || message.contains("expire")) {
            AuthAlertType.TokenExpired
        } else {
            AuthAlertType.InvalidToken
        }
    }

    // ==================== LEGACY SUPPORT ====================

    /**
     * Legacy method for setting logged in state
     */
    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    /**
     * Legacy method for getting logged in state
     */
    fun getLoggedIn(): Boolean {
        return _isLoggedIn.value
    }
}
