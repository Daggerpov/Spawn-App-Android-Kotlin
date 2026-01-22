package com.example.spawn_app_android.data.model

import com.google.gson.annotations.SerializedName

data class OAuthSignInRequest(
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("provider")
    val provider: String
)
