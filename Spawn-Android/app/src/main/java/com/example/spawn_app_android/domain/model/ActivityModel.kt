package com.example.spawn_app_android.domain.model

import java.util.UUID

data class ActivityModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Sample Activity",
    val host: String = "John Doe",
    val time: String = "12:00",
    val location: String = "Breka",
    val distance: String = "2km",
    val tag: String = "EAT", // "EAT", "GYM", "STUDY"
    val status: String = "HAPPENING NOW" // "HAPPENING NOW", "IN 3 HOURS"
)


