package com.example.spawn_app_android.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponseDTO(
    @SerializedName("user")
    val user: BaseUserDTO,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("isOAuthUser")
    val isOAuthUser: Boolean? = null
) {
    fun getUserStatus(): UserStatus? {
        return status?.let { UserStatus.fromValue(it) }
    }
}
