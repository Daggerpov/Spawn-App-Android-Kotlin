package com.example.spawn_app_android.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class BaseUserDTO(
    @SerializedName("id")
    val id: UUID,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("hasCompletedOnboarding")
    val hasCompletedOnboarding: Boolean? = null
)
