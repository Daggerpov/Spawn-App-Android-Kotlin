package com.example.spawn_app_android.data.model

enum class AuthProviderType(val value: String) {
    GOOGLE("google"),
    APPLE("apple"),
    EMAIL("email");

    companion object {
        fun fromValue(value: String): AuthProviderType? {
            return entries.find { it.value == value }
        }
    }
}
