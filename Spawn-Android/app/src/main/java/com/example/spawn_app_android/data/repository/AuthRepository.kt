package com.example.spawn_app_android.data.repository

import android.content.Context
import android.util.Log
import com.example.spawn_app_android.data.local.TokenManager
import com.example.spawn_app_android.data.model.AuthProviderType
import com.example.spawn_app_android.data.model.AuthResponseDTO
import com.example.spawn_app_android.data.model.OAuthRegistrationDTO
import com.example.spawn_app_android.data.remote.ApiClient

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: Int? = null) : AuthResult<Nothing>()
}

class AuthRepository(private val context: Context) {

    private val authApiService = ApiClient.getAuthApiService(context)
    private val tokenManager = TokenManager.getInstance(context)

    suspend fun signInWithGoogle(
        idToken: String,
        email: String?,
        displayName: String? = null,
        profilePictureUrl: String? = null
    ): AuthResult<AuthResponseDTO> {
        return try {
            Log.d("AuthRepository", "Attempting Google sign-in with email: $email")
            
            val response = authApiService.signInWithOAuth(
                idToken = idToken,
                email = email ?: "",
                provider = AuthProviderType.GOOGLE.value
            )

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Sign-in successful for user: ${authResponse.user.username}")
                    AuthResult.Success(authResponse)
                } else {
                    Log.e("AuthRepository", "Sign-in response body is null")
                    AuthResult.Error("Empty response from server")
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Sign-in failed - Code: $errorCode, Body: $errorBody")
                
                // If user doesn't exist (402 or 404), try to register them
                if (errorCode == 402 || errorCode == 404) {
                    Log.d("AuthRepository", "User not found, attempting registration...")
                    return registerWithGoogle(idToken, email, displayName, profilePictureUrl)
                }
                
                val errorMessage = when (errorCode) {
                    401 -> "Authentication failed. Please try again."
                    else -> "Sign-in failed (Error $errorCode)"
                }
                AuthResult.Error(errorMessage, errorCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-in exception", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun registerWithGoogle(
        idToken: String,
        email: String?,
        name: String?,
        profilePictureUrl: String?
    ): AuthResult<AuthResponseDTO> {
        return try {
            Log.d("AuthRepository", "Attempting Google registration with email: $email")
            
            val registrationDTO = OAuthRegistrationDTO(
                idToken = idToken,
                provider = AuthProviderType.GOOGLE.value,
                email = email,
                name = name,
                profilePictureUrl = profilePictureUrl
            )
            
            val response = authApiService.registerWithOAuth(registrationDTO)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Registration successful for user: ${authResponse.user.username}")
                    AuthResult.Success(authResponse)
                } else {
                    Log.e("AuthRepository", "Registration response body is null")
                    AuthResult.Error("Empty response from server")
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Registration failed - Code: $errorCode, Body: $errorBody")
                
                val errorMessage = when (errorCode) {
                    409 -> "An account with this email already exists."
                    else -> "Registration failed (Error $errorCode)"
                }
                AuthResult.Error(errorMessage, errorCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration exception", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun quickSignIn(): AuthResult<AuthResponseDTO> {
        return try {
            if (!tokenManager.hasTokens()) {
                return AuthResult.Error("No tokens available", 401)
            }

            Log.d("AuthRepository", "Attempting quick sign-in")
            val response = authApiService.quickSignIn()

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Quick sign-in successful")
                    AuthResult.Success(authResponse)
                } else {
                    AuthResult.Error("Empty response from server")
                }
            } else {
                val errorCode = response.code()
                Log.e("AuthRepository", "Quick sign-in failed with code: $errorCode")
                if (errorCode == 401) {
                    // Token expired, clear tokens
                    tokenManager.clearTokens()
                }
                AuthResult.Error("Session expired", errorCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Quick sign-in exception", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    fun signOut() {
        tokenManager.clearTokens()
        ApiClient.clearInstance()
        Log.d("AuthRepository", "User signed out, tokens cleared")
    }

    fun hasStoredTokens(): Boolean {
        return tokenManager.hasTokens()
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
