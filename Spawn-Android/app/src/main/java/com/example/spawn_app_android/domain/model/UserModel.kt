package com.example.spawn_app_android.domain.model

import java.util.UUID

data class User(
    var id: UUID,
    var friendUserIds: List<UUID>,
    var username: String,
    var profilePicture: String,
    var name: String?,
    var bio: String?,
    var email: String,
)