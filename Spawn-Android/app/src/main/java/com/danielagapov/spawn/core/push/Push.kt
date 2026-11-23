package com.danielagapov.spawn.core.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.danielagapov.spawn.MainActivity
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.model.DeviceTokenDTO
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.session.AuthState
import com.danielagapov.spawn.core.session.Spawn
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
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { Log.w(TAG, "Couldn't delete the FCM token", it) }
    }

    /** Shows a notification. The system does this itself when the app is in the background. */
    fun show(context: Context, title: String?, body: String?) {
        if (title.isNullOrBlank() && body.isNullOrBlank()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.activity_indigo))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }
}
