package com.danielagapov.spawn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

enum class BannerKind { Success, Error, Notification }

data class BannerMessage(
    val kind: BannerKind,
    val text: String,
    val title: String? = null,
    val senderId: String? = null,
    val id: Long = System.nanoTime(),
)

/**
 * App-wide top banners from Figma's "In-app Notifications" page (Success 2211:7536, Error 2211:7735),
 * replacing system toasts and inline error text for one-off results of user actions.
 */
object InAppBanners {
    var current by mutableStateOf<BannerMessage?>(null)
        private set

    /** Hosts drawn in other windows (the activity sheet) take over while they're on screen. */
    internal var overlayHosts by mutableIntStateOf(0)

    fun success(text: String) { current = BannerMessage(BannerKind.Success, text) }

    fun error(text: String) { current = BannerMessage(BannerKind.Error, text) }

    fun notification(title: String?, body: String?, senderId: String?) {
        val text = body?.takeIf(String::isNotBlank) ?: title?.takeIf(String::isNotBlank) ?: return
        current = BannerMessage(
            BannerKind.Notification,
            text,
            title = title?.takeIf { it.isNotBlank() && it != text },
            senderId = senderId,
        )
    }

    fun clearNotification() {
        if (current?.kind == BannerKind.Notification) current = null
    }


    fun dismiss(id: Long) { if (current?.id == id) current = null }
}

/**
 * Shows [InAppBanners.current] at the top of the screen for a few seconds; tap to dismiss.
 * The root host sits over the whole app; pass [overlay] = true for a host inside a sheet or dialog window,
 * and [statusBarInset] = false when that window doesn't reach the status bar.
 */
@Composable
fun InAppBannerHost(modifier: Modifier = Modifier, overlay: Boolean = false, statusBarInset: Boolean = true) {
    if (overlay) {
        DisposableEffect(Unit) {
            InAppBanners.overlayHosts++
            onDispose { InAppBanners.overlayHosts-- }
        }
    }
    val message = InAppBanners.current
    val active = overlay || InAppBanners.overlayHosts == 0
    var shown by remember { mutableStateOf<BannerMessage?>(null) }
    if (message != null) shown = message
    LaunchedEffect(message?.id) {
        if (message != null) {
            delay(if (message.kind == BannerKind.Error) 4000 else 3000)
            InAppBanners.dismiss(message.id)
        }
    }
    AnimatedVisibility(
        visible = message != null && active,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        shown?.let { Banner(it, statusBarInset) }
    }
}

@Composable
private fun Banner(m: BannerMessage, statusBarInset: Boolean) {
    if (m.kind == BannerKind.Notification) {
        NotificationBanner(m, statusBarInset)
        return
    }
    val success = m.kind == BannerKind.Success
    val background = if (success) Color(0xFFB0FBDD) else Color(0xFFFFD4DB)
    val stroke = if (success) Color(0xFF71CCAC) else Color(0xFFFF8DA0)
    val label = if (success) Color(0xFF4CB08F) else Color(0xFFFF627C)
    val content = if (success) Color(0xFF339E7C) else Color(0xFFFF4664)
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (statusBarInset) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, shape, spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(background)
            .border(1.dp, stroke, shape)
            .clickable { InAppBanners.dismiss(m.id) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (success) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline, null, tint = content, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(if (success) "Success!" else "Alert", style = onest(12.sp), color = label)
            Text(m.text, style = onest(16.sp, FontWeight.SemiBold), color = content, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Regular notification variant from Figma 2211:7528. */
@Composable
private fun NotificationBanner(message: BannerMessage, statusBarInset: Boolean) {
    val colors = SpawnTheme.colors
    val sender by produceState<BaseUserDTO?>(null, message.id) {
        value = null
        val senderId = message.senderId ?: return@produceState
        try {
            value = Spawn.api.getUser(senderId, Spawn.userId).body()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep the standard notification icon if the sender is unavailable.
        }
    }
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.fillMaxWidth()
            .then(if (statusBarInset) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(colors.surfaceMuted)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .clickable(onClickLabel = "Dismiss notification") { InAppBanners.dismiss(message.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val user = sender
        if (user == null) {
            Icon(Icons.Outlined.Notifications, null, tint = colors.textSecondary, modifier = Modifier.size(34.dp))
        } else {
            UserAvatar(user, 34.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            message.title?.let { Text(it, style = onest(11.sp, FontWeight.Medium), color = colors.textSecondary) }
            Text(message.text, style = onest(16.sp, FontWeight.Bold), color = colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
