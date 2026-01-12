package com.example.spawn_app_android.data.remote

import android.content.Context
import com.example.spawn_app_android.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://spawn-app-back-end-production.up.railway.app/api/v1/"

    @Volatile
    private var retrofitInstance: Retrofit? = null

    @Volatile
    private var authApiService: AuthApiService? = null

    fun getInstance(context: Context): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: buildRetrofit(context).also { retrofitInstance = it }
        }
    }

    fun getAuthApiService(context: Context): AuthApiService {
        return authApiService ?: synchronized(this) {
            authApiService ?: getInstance(context).create(AuthApiService::class.java).also { authApiService = it }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val tokenManager = TokenManager.getInstance(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(TokenResponseInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun clearInstance() {
        retrofitInstance = null
        authApiService = null
    }
}