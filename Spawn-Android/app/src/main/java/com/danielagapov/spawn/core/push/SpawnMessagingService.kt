package com.danielagapov.spawn.core.push

import com.danielagapov.spawn.core.session.Spawn
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SpawnMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = Push.onNewToken(token)

    /** Only called while the app is in the foreground; background notifications are shown by the system. */
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        Push.show(this, notification.title, notification.body)
        Spawn.feedChanged()
    }
}
