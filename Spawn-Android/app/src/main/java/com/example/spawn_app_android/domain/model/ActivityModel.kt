package com.example.spawn_app_android.domain.model

data class ActivityModel(
    val id: String,
    val title: String,
    val host: String,
    val time: String,
    val location: String,
    val distance: String,
    val tag: String, // "EAT", "GYM", "STUDY"
    val status: String // "HAPPENING NOW", "IN 3 HOURS"
)


