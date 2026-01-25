package com.example.spawn_app_android.data.model

import com.google.gson.annotations.SerializedName

data class OAuthRegistrationDTO(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("provider")
    val provider: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("profilePictureUrl")
    val profilePictureUrl: String? = null
)
