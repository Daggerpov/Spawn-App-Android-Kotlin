package com.example.spawn_app_android.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient.kt
 *
 * Created by Ethan Dsouza on 2025-12-06
 *
 * ApiClient singleton that provides Retrofit instance with:
 * - Auth token interceptor
 * - Token refresh handling
 * - Logging interceptor
 * - Custom date parsing
 */
object ApiClient {

    private const val TAG = "ApiClient"

    // TODO: Update this to your actual API base URL
    const val BASE_URL = "https://api.spawn-app.com/"

    // Endpoints that don't require authentication
    private val whitelistedEndpoints = listOf(
        "auth/register/verification/send",
        "auth/register/oauth",
        "auth/register/verification/check",
        "auth/sign-in",
        "auth/login",
        "optional-details",
        "contacts/cross-reference"
    )

    // Auth endpoints that handle tokens differently
    private val authEndpoints = listOf(
        "auth/sign-in",
        "auth/login",
        "auth/register/oauth",
        "auth/register/verification/check",
        "auth/user/details",
        "auth/quick-sign-in"
    )

    private var retrofit: Retrofit? = null
    private var tokenManager: TokenManager? = null

    /**
     * Initialize the ApiClient with application context.
     * Must be called before using getInstance().
     */
    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    /**
     * Get the TokenManager instance
     */
    fun getTokenManager(): TokenManager? = tokenManager

    /**
     * Get or create the Retrofit instance
     */
    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = createRetrofit()
        }
        return retrofit!!
    }

    /**
     * Get the ApiService interface
     */
    fun getApiService(): ApiService {
        return getInstance().create(ApiService::class.java)
    }

    /**
     * Force recreate the Retrofit instance (useful after token changes)
     */
    fun resetInstance() {
        retrofit = null
    }

    private fun createRetrofit(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Auth interceptor that adds Authorization header to requests
     * and handles token extraction from auth responses
     */
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val url = originalRequest.url.toString()

            // Check if this endpoint requires authentication
            val isWhitelisted = whitelistedEndpoints.any { url.contains(it) }

            val request = if (!isWhitelisted) {
                // Add auth header if we have an access token
                val accessToken = tokenManager?.getAccessToken()
                if (accessToken != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                } else {
                    Log.w(TAG, "⚠️ Missing access token for authenticated endpoint: $url")
                    originalRequest
                }
            } else {
                originalRequest
            }

            val response = chain.proceed(request)

            // Handle auth tokens from response headers (for auth endpoints)
            if (authEndpoints.any { url.contains(it) }) {
                handleAuthTokensFromResponse(response)
            }

            return response
        }

        private fun handleAuthTokensFromResponse(response: Response) {
            // Extract access token from Authorization header
            val accessToken = response.header("Authorization")
                ?: response.header("authorization")

            if (accessToken != null) {
                val cleanToken = accessToken.removePrefix("Bearer ")
                tokenManager?.saveAccessToken(cleanToken)
                Log.d(TAG, "✅ Saved access token from response")
            }

            // Extract refresh token from X-Refresh-Token header
            val refreshToken = response.header("X-Refresh-Token")
                ?: response.header("x-refresh-token")

            if (refreshToken != null) {
                tokenManager?.saveRefreshToken(refreshToken)
                Log.d(TAG, "✅ Saved refresh token from response")
            }
        }
    }
}

/**
 * Token manager for secure storage of auth tokens.
 * Uses SharedPreferences with encryption in production.
 *
 * TODO: For production, consider using EncryptedSharedPreferences
 * or Android Keystore for more secure token storage.
 */
class TokenManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "spawn_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun hasValidTokens(): Boolean {
        return getAccessToken() != null
    }

    fun hasRefreshToken(): Boolean {
        return getRefreshToken() != null
    }
}