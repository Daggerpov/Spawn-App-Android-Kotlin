package com.example.spawn_app_android.data.remote

import com.example.spawn_app_android.data.model.AuthResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthApiService {

    @GET("auth/sign-in")
    suspend fun signInWithOAuth(
        @Query("idToken") idToken: String,
        @Query("email") email: String,
        @Query("provider") provider: String
    ): Response<AuthResponseDTO>

    @GET("auth/quick-sign-in")
    suspend fun quickSignIn(): Response<AuthResponseDTO>
}
