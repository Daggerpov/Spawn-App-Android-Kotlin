package com.danielagapov.spawn.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.model.DeviceTokenDTO
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.components.InAppBanners
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Push notifications through Firebase Cloud Messaging (project spawn-e806a), like iOS.
 * The FCM token is registered with the backend (POST notifications/device-tokens) whenever someone is
 * signed in, and the backend sends every push through FCM.
 */
object Push {
    const val CHANNEL_ID = "spawn_default"
    private const val TAG = "Push"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** The (userId, token) pair the backend last accepted, so repeated auth updates don't re-post. */
    @Volatile
    private var registered: Pair<String, String>? = null

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Only fully onboarded users: the backend rejects device tokens from accounts still onboarding. */
    private val activeUserId: String?
        get() = (Spawn.auth.value as? AuthState.SignedIn)?.user?.id

    /** Registers this device's FCM token for whoever is signed in. */
    fun syncToken() {
        val userId = activeUserId ?: return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> register(userId, token) }
            .addOnFailureListener { Log.w(TAG, "Couldn't get an FCM token", it) }
    }

    /** FCM rotated the token. */
    fun onNewToken(token: String) {
        activeUserId?.let { register(it, token) }
    }

    private fun register(userId: String, token: String) {
        if (registered == userId to token) return
        scope.launch {
            runCatching { Spawn.api.registerDeviceToken(DeviceTokenDTO(token, "ANDROID", userId)).bodyOrThrow() }
                .onSuccess { registered = userId to token }
                .onFailure { Log.w(TAG, "Registering the push token failed", it) }
        }
    }

    /** Call while still signed in (before clearing tokens) so this device stops getting the account's pushes. */
    suspend fun unregister() {
        val (userId, token) = registered ?: return
        registered = null
        withTimeoutOrNull(5_000) {
            runCatching { Spawn.api.unregisterDeviceToken(DeviceTokenDTO(token, "ANDROID", userId)).bodyOrThrow() }
                .onFailure { Log.w(TAG, "Unregistering the push token failed", it) }
        }
    }

    /** After any sign-out (including an expired session), drop the token so the old account can't reach us. */
    fun onSignedOut() {
        registered = null
        InAppBanners.clearNotification()
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { Log.w(TAG, "Couldn't delete the FCM token", it) }
    }

}
