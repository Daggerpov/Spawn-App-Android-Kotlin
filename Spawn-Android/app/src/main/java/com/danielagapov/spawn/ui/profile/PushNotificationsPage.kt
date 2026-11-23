package com.danielagapov.spawn.ui.profile

import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.danielagapov.spawn.core.push.Push
import com.danielagapov.spawn.ui.components.ErrorBanner
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

/** Figma Push Notifications, 455:6483. */
@Composable
internal fun PushNotificationsPage(userId: String, vm: MyProfileViewModel, bottomPadding: Dp, onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = SpawnTheme.colors
    var enabled by remember { mutableStateOf(false) }
    LifecycleResumeEffect(context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        enabled = NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            manager.getNotificationChannel(Push.CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
        onPauseOrDispose {}
    }
    LaunchedEffect(userId) { vm.loadNotificationPreferences() }
    val preferences = vm.notificationPreferences?.takeIf { it.userId == userId }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubPageHeader("Push Notifications", onBack)
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = bottomPadding + 16.dp)) {
            Column(Modifier.fillMaxWidth().background(colors.surfaceMuted, RoundedCornerShape(12.dp)).padding(16.dp)) {
                Text("PUSH NOTIFICATION STATUS", style = onest(11.sp, FontWeight.Medium), color = colors.textSecondary)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (enabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        tint = if (enabled) Palette.Green500 else colors.textSecondary,
                    )
                    Column {
                        Text(
                            if (enabled) "Notifications Enabled" else "Notifications Disabled",
                            style = onest(18.sp, FontWeight.SemiBold), color = colors.heading,
                        )
                        Text(
                            if (enabled) "Android allows notifications from Spawn."
                            else "Allow notifications in Android settings.",
                            style = onest(12.sp), color = colors.textSecondary,
                        )
                    }
                }
                if (!enabled) {
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    }) { Text("Open Android settings") }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Notification Types", style = onest(13.sp, FontWeight.Medium), color = colors.textSecondary)
            vm.notificationPreferencesError?.let { ErrorBanner(it) }
            if (preferences == null) {
                if (vm.notificationPreferencesError != null) {
                    TextButton(onClick = vm::loadNotificationPreferences) { Text("Retry") }
                } else {
                    CircularProgressIndicator(Modifier.padding(top = 16.dp).size(24.dp))
                }
            } else {
                val canSave = !vm.notificationPreferencesBusy
                NotificationToggle("Friend Requests", preferences.friendRequestsEnabled, canSave) {
                    vm.saveNotificationPreferences(preferences.copy(friendRequestsEnabled = it))
                }
                NotificationToggle("Event Invites", preferences.activityInvitesEnabled, canSave) {
                    vm.saveNotificationPreferences(preferences.copy(activityInvitesEnabled = it))
                }
                NotificationToggle("Event Updates", preferences.activityUpdatesEnabled, canSave) {
                    vm.saveNotificationPreferences(preferences.copy(activityUpdatesEnabled = it))
                }
                NotificationToggle("Event Chat Messages", preferences.chatMessagesEnabled, canSave) {
                    vm.saveNotificationPreferences(preferences.copy(chatMessagesEnabled = it))
                }
            }
        }
    }
}

@Composable
private fun NotificationToggle(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val colors = SpawnTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = onest(17.sp), color = colors.text)
        Switch(
            checked = checked, onCheckedChange = null, enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = Palette.Green500, checkedThumbColor = colors.background),
        )
    }
}
