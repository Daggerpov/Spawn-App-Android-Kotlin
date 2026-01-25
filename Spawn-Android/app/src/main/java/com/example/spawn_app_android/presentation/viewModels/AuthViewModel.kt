package com.example.spawn_app_android.presentation.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spawn_app_android.data.model.AuthProviderType
import com.example.spawn_app_android.data.model.AuthResponseDTO
import com.example.spawn_app_android.data.model.BaseUserDTO
import com.example.spawn_app_android.data.model.UserStatus
import com.example.spawn_app_android.data.repository.AuthRepository
import com.example.spawn_app_android.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: BaseUserDTO, val status: UserStatus?) : AuthState()
    data class NeedsOnboarding(val status: UserStatus) : AuthState()
    object UserNotFound : AuthState()  // User tried OAuth but doesn't have a Spawn account
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

data class OnboardingState(
    val idToken: String? = null,
    val provider: AuthProviderType? = null,
    val email: String? = null,
    val name: String? = null,
    val profilePictureUrl: String? = null,
    val isComplete: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<BaseUserDTO?>(null)
    val currentUser: StateFlow<BaseUserDTO?> = _currentUser.asStateFlow()

    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

    private var authRepository: AuthRepository? = null

    fun initialize(context: Context) {
        if (authRepository == null) {
            authRepository = AuthRepository.getInstance(context)
        }
    }

    fun checkExistingSession(context: Context) {
        initialize(context)
        viewModelScope.launch {
            val repo = authRepository ?: return@launch

            if (!repo.hasStoredTokens()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            _authState.value = AuthState.Loading

            when (val result = repo.quickSignIn()) {
                is AuthResult.Success -> {
                    handleAuthSuccess(result.data)
                }
                is AuthResult.Error -> {
                    Log.d("AuthViewModel", "Quick sign-in failed: ${result.message}")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        idToken: String,
        email: String?,
        displayName: String? = null,
        profilePictureUrl: String? = null
    ) {
        initialize(context)
        
        // Store onboarding data in case we need it
        setOnboardingData(
            idToken = idToken,
            provider = AuthProviderType.GOOGLE,
            email = email,
            name = displayName,
            profilePictureUrl = profilePictureUrl
        )
        
        viewModelScope.launch {
            val repo = authRepository ?: return@launch

            _authState.value = AuthState.Loading

            when (val result = repo.signInWithGoogle(idToken, email, displayName, profilePictureUrl)) {
                is AuthResult.Success -> {
                    handleAuthSuccess(result.data)
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "Google sign-in failed: ${result.message}")
                    // If 404, user doesn't exist - show AccountNotFound screen
                    if (result.code == 404) {
                        _authState.value = AuthState.UserNotFound
                    } else {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    private fun handleAuthSuccess(authResponse: AuthResponseDTO) {
        _currentUser.value = authResponse.user
        val userStatus = authResponse.getUserStatus()

        Log.d("AuthViewModel", "Auth success - User: ${authResponse.user.username}, Status: $userStatus")

        // Check if user needs onboarding
        if (userStatus != null && userStatus != UserStatus.ACTIVE) {
            Log.d("AuthViewModel", "User needs onboarding, status: $userStatus")
            _authState.value = AuthState.NeedsOnboarding(userStatus)
        } else {
            _authState.value = AuthState.Authenticated(authResponse.user, userStatus)
        }
    }

    fun setOnboardingData(
        idToken: String,
        provider: AuthProviderType,
        email: String?,
        name: String?,
        profilePictureUrl: String?
    ) {
        _onboardingState.value = OnboardingState(
            idToken = idToken,
            provider = provider,
            email = email,
            name = name,
            profilePictureUrl = profilePictureUrl
        )
    }

    fun createUser(
        context: Context,
        username: String,
        name: String,
        profilePictureUri: Uri?
    ) {
        initialize(context)
        viewModelScope.launch {
            val repo = authRepository ?: return@launch
            val state = _onboardingState.value
            
            if (state.idToken == null || state.provider == null) {
                _onboardingState.value = state.copy(error = "Missing authentication data")
                return@launch
            }

            when (val result = repo.createUser(
                username = username,
                name = name,
                email = state.email,
                idToken = state.idToken,
                provider = state.provider,
                profilePictureUri = profilePictureUri,
                profilePictureUrl = state.profilePictureUrl
            )) {
                is AuthResult.Success -> {
                    Log.d("AuthViewModel", "User created successfully: ${result.data.username}")
                    _currentUser.value = result.data
                    _onboardingState.value = state.copy(isComplete = true, error = null)
                    _authState.value = AuthState.Authenticated(result.data, UserStatus.ACTIVE)
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "Create user failed: ${result.message}")
                    _onboardingState.value = state.copy(error = result.message)
                }
            }
        }
    }

    fun signOut(context: Context) {
        initialize(context)
        authRepository?.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun proceedToOnboarding() {
        // Called when user clicks "Register Now" on AccountNotFoundScreen
        // Transition from UserNotFound to NeedsOnboarding to go to the registration form
        _authState.value = AuthState.NeedsOnboarding(UserStatus.EMAIL_VERIFIED)
    }

    fun returnToLogin() {
        // Called when user clicks "Return to Login" on AccountNotFoundScreen
        // Clear onboarding data and return to unauthenticated state
        _onboardingState.value = OnboardingState()
        _authState.value = AuthState.Unauthenticated
    }

    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    // Legacy compatibility methods
    fun getLoggedIn(): Boolean {
        return isAuthenticated()
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        // Legacy method - no-op, use proper auth methods instead
    }
}
