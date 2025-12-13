package com.example.spawn_app_android.domain.model

data class Friend(
    val firstName: String,
    val lastName: String,
    val username: String,
    val profilePictureUrl: String? = null
)