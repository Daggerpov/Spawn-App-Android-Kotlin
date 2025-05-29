package com.example.spawn_app_android.data.remote


import com.example.spawn_app_android.data.model.EventsDTO
import retrofit2.http.GET

interface ApiService {
    @GET("a711ac31-ed90-4e39-8f84-7485209660b")
    suspend fun getEvents(): EventsDTO

}