package com.example.spawn_app_android.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.spawn_app_android.data.local.TokenManager
import com.example.spawn_app_android.data.model.AuthProviderType
import com.example.spawn_app_android.data.model.AuthResponseDTO
import com.example.spawn_app_android.data.model.BaseUserDTO
import com.example.spawn_app_android.data.model.OAuthRegistrationDTO
import com.example.spawn_app_android.data.model.OAuthSignInRequest
import com.example.spawn_app_android.data.model.UserCreateDTO
import com.example.spawn_app_android.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL

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
            Log.d("AuthRepository", "Token length: ${idToken.length}")
            
            // Build URL exactly like iOS does with URLComponents + URLQueryItem
            // iOS uses URLQueryItem which handles encoding automatically
            val baseUrl = "https://spawn-app-back-end-production.up.railway.app/api/v1/auth/sign-in"
            val uri = android.net.Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("idToken", idToken)
                .appendQueryParameter("email", email ?: "")
                .appendQueryParameter("provider", AuthProviderType.GOOGLE.value)
                .build()
            
            Log.d("AuthRepository", "Request URL (first 200 chars): ${uri.toString().take(200)}...")
            
            // Make direct OkHttp call matching iOS URLSession behavior
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val request = okhttp3.Request.Builder()
                .url(uri.toString())
                .get()
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            
            val responseBody = response.body?.string()
            Log.d("AuthRepository", "Response code: ${response.code}")
            
            if (response.isSuccessful && responseBody != null) {
                val gson = com.google.gson.Gson()
                val authResponse = gson.fromJson(responseBody, AuthResponseDTO::class.java)
                
                // Store tokens from response headers (matching iOS handleAuthTokens)
                response.header("Authorization")?.let { accessToken ->
                    val cleanToken = accessToken.removePrefix("Bearer ")
                    tokenManager.saveAccessToken(cleanToken)
                    Log.d("AuthRepository", "Saved access token")
                }
                response.header("X-Refresh-Token")?.let { refreshToken ->
                    tokenManager.saveRefreshToken(refreshToken)
                    Log.d("AuthRepository", "Saved refresh token")
                }
                
                Log.d("AuthRepository", "Sign-in successful for user: ${authResponse.user.username}")
                AuthResult.Success(authResponse)
            } else {
                val errorCode = response.code
                Log.e("AuthRepository", "Sign-in failed - Code: $errorCode, Body: $responseBody")
                
                // If user doesn't exist (404), return error with code so ViewModel can navigate to onboarding
                val errorMessage = when (errorCode) {
                    404 -> "User not found"
                    401 -> {
                        Log.e("AuthRepository", "401 Error - Backend rejected token")
                        val backendMessage = try {
                            if (!responseBody.isNullOrEmpty()) {
                                val json = JSONObject(responseBody)
                                json.optString("message", "").ifEmpty { null }
                            } else null
                        } catch (e: Exception) {
                            responseBody
                        }
                        
                        if (!backendMessage.isNullOrEmpty()) {
                            Log.e("AuthRepository", "Backend error message: $backendMessage")
                            "Authentication failed: $backendMessage"
                        } else {
                            "Authentication failed. Token may be invalid or expired."
                        }
                    }
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

    suspend fun createUser(
        username: String,
        name: String,
        email: String?,
        idToken: String,
        provider: AuthProviderType,
        profilePictureUri: Uri?,
        profilePictureUrl: String?
    ): AuthResult<BaseUserDTO> {
        return try {
            Log.d("AuthRepository", "Creating user with username: $username")
            
            // Convert profile picture to base64 if provided
            var profilePictureData: String? = null
            
            if (profilePictureUri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(profilePictureUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val bytes = outputStream.toByteArray()
                    profilePictureData = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    Log.d("AuthRepository", "Encoded local profile picture: ${bytes.size} bytes")
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to encode profile picture: ${e.message}")
                }
            } else if (!profilePictureUrl.isNullOrEmpty()) {
                // Download Google/Apple profile picture and convert to base64
                try {
                    profilePictureData = withContext(Dispatchers.IO) {
                        val url = URL(profilePictureUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        val inputStream = connection.getInputStream()
                        val bytes = inputStream.readBytes()
                        inputStream.close()
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                    Log.d("AuthRepository", "Downloaded and encoded provider profile picture")
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to download provider profile picture: ${e.message}")
                }
            }
            
            val userDTO = UserCreateDTO(
                username = username,
                name = name,
                email = email,
                profilePictureData = profilePictureData,
                profilePictureUrl = profilePictureUrl
            )
            
            val response = authApiService.makeUser(
                userDTO = userDTO,
                idToken = idToken,
                provider = provider.value
            )
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d("AuthRepository", "User created successfully: ${user.username}")
                    AuthResult.Success(user)
                } else {
                    Log.e("AuthRepository", "Create user response body is null")
                    AuthResult.Error("Empty response from server")
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Create user failed - Code: $errorCode, Body: $errorBody")
                
                val errorMessage = when (errorCode) {
                    409 -> "Username is already taken"
                    401 -> "Authentication failed. Please try signing in again."
                    else -> "Failed to create user (Error $errorCode)"
                }
                AuthResult.Error(errorMessage, errorCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Create user exception", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
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
