package com.example.spawn_app_android.presentation.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<BaseUserDTO?>(null)
    val currentUser: StateFlow<BaseUserDTO?> = _currentUser.asStateFlow()

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
        viewModelScope.launch {
            val repo = authRepository ?: return@launch

            _authState.value = AuthState.Loading

            when (val result = repo.signInWithGoogle(idToken, email, displayName, profilePictureUrl)) {
                is AuthResult.Success -> {
                    handleAuthSuccess(result.data)
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "Google sign-in failed: ${result.message}")
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    private fun handleAuthSuccess(authResponse: AuthResponseDTO) {
        _currentUser.value = authResponse.user
        val userStatus = authResponse.getUserStatus()

        Log.d("AuthViewModel", "Auth success - User: ${authResponse.user.username}, Status: $userStatus")

        _authState.value = AuthState.Authenticated(authResponse.user, userStatus)
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
