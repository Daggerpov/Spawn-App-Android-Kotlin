package com.example.spawn_app_android.data.remote

import android.util.Log
import com.example.spawn_app_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenResponseInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val authEndpoints = listOf(
        "auth/sign-in",
        "auth/login",
        "auth/register/oauth",
        "auth/register/verification/check",
        "auth/user/details",
        "auth/quick-sign-in"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()

        // Only extract tokens for auth endpoints
        if (authEndpoints.any { url.contains(it) }) {
            extractAndSaveTokens(response)
        }

        return response
    }

    private fun extractAndSaveTokens(response: Response) {
        // Extract access token from Authorization header
        val authHeader = response.header("Authorization") ?: response.header("authorization")
        if (authHeader != null) {
            val accessToken = authHeader.removePrefix("Bearer ").trim()
            tokenManager.saveAccessToken(accessToken)
            Log.d("TokenInterceptor", "Access token saved")
        }

        // Extract refresh token from X-Refresh-Token header
        val refreshToken = response.header("X-Refresh-Token") ?: response.header("x-refresh-token")
        if (refreshToken != null) {
            tokenManager.saveRefreshToken(refreshToken)
            Log.d("TokenInterceptor", "Refresh token saved")
        }
    }
}
