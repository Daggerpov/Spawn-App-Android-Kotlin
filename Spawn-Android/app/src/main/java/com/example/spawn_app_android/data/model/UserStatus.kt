package com.example.spawn_app_android.data.model

enum class UserStatus(val value: String) {
    EMAIL_VERIFIED("EMAIL_VERIFIED"),
    USERNAME_AND_PHONE_NUMBER("USERNAME_AND_PHONE_NUMBER"),
    NAME_AND_PHOTO("NAME_AND_PHOTO"),
    CONTACT_IMPORT("CONTACT_IMPORT"),
    ACTIVE("ACTIVE");

    companion object {
        fun fromValue(value: String): UserStatus? {
            return entries.find { it.value == value }
        }
    }
}
