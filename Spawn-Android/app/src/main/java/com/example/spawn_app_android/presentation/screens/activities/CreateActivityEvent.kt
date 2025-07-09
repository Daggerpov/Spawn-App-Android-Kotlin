package com.example.spawn_app_android.presentation.screens.activities

import java.time.LocalDateTime

sealed class CreateActivityEvent {
    data class TitleChanged(val title: String) : CreateActivityEvent()
    data class DescriptionChanged(val description: String) : CreateActivityEvent()
    data class LocationChanged(val location: String) : CreateActivityEvent()
    data class TimeChanged(val time: LocalDateTime) : CreateActivityEvent()
    data class DurationChanged(val duration: String) : CreateActivityEvent()
    object Submit : CreateActivityEvent()
}