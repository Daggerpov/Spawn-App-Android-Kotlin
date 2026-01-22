package com.example.spawn_app_android.data.remote

import com.example.spawn_app_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val whitelistedEndpoints = listOf(
        "auth/register/verification/send",
        "auth/register/oauth",
        "auth/register/verification/check",
        "auth/sign-in",
        "auth/login",
        "auth/make-user"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Skip auth header for whitelisted endpoints
        if (whitelistedEndpoints.any { url.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
