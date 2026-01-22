package com.example.spawn_app_android.data.remote

import com.example.spawn_app_android.data.model.AuthResponseDTO
import com.example.spawn_app_android.data.model.BaseUserDTO
import com.example.spawn_app_android.data.model.OAuthRegistrationDTO
import com.example.spawn_app_android.data.model.OAuthSignInRequest
import com.example.spawn_app_android.data.model.UserCreateDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @GET("auth/sign-in")
    suspend fun signInWithOAuth(
        @Query("idToken") idToken: String,
        @Query("email") email: String,
        @Query("provider") provider: String
    ): Response<AuthResponseDTO>

    @POST("auth/register/oauth")
    suspend fun registerWithOAuth(
        @Body registrationDTO: OAuthRegistrationDTO
    ): Response<AuthResponseDTO>

    @GET("auth/quick-sign-in")
    suspend fun quickSignIn(): Response<AuthResponseDTO>

    @POST("auth/make-user")
    suspend fun makeUser(
        @Body userDTO: UserCreateDTO,
        @Query("idToken") idToken: String,
        @Query("provider") provider: String
    ): Response<BaseUserDTO>
}
