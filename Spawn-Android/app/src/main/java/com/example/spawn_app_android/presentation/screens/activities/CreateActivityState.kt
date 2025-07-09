package com.example.spawn_app_android.presentation.screens.activities

import java.time.LocalDateTime

data class CreateActivityState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val dateTime: LocalDateTime? = null,
    val isSubmitting: Boolean = false,
    val duration: String = "",
    val error: String? = null
)
