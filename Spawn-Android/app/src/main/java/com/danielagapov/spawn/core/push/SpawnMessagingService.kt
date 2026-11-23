package com.danielagapov.spawn.core.push

import androidx.core.content.ContextCompat
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.components.InAppBanners
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SpawnMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = Push.onNewToken(token)

    /** FCM handles background display for the server's notification payloads. */
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        val user = (Spawn.auth.value as? AuthState.SignedIn)?.user ?: return
        val senderId = message.data["senderId"] ?: message.data["creatorId"] ?: message.data["receiverId"]
        ContextCompat.getMainExecutor(this).execute {
            if (Spawn.userId == user.id) {
                InAppBanners.notification(notification.title, notification.body, senderId)
                Spawn.feedChanged()
            }
        }
    }
}
