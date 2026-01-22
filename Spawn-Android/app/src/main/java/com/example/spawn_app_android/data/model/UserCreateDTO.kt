package com.example.spawn_app_android.data.model

import com.google.gson.annotations.SerializedName

data class UserCreateDTO(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("profilePictureData")
    val profilePictureData: String? = null,
    
    @SerializedName("profilePictureUrl")
    val profilePictureUrl: String? = null
)
