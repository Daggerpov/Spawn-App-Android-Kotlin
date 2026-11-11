package com.danielagapov.spawn.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.time.YearMonth

/** Your own profile + settings (iOS ProfileViewModel / EditProfileView / SettingsView). */
class MyProfileViewModel : ViewModel() {
    var stats by mutableStateOf<UserStatsDTO?>(null); private set
    var interests by mutableStateOf<List<String>>(emptyList()); private set
    var socials by mutableStateOf<UserSocialMediaDTO?>(null); private set
    var month by mutableStateOf(YearMonth.now()); private set
    var calendar by mutableStateOf<List<CalendarActivityDTO>>(emptyList()); private set
    var blocked by mutableStateOf<List<BlockedUserDTO>>(emptyList()); private set
    var saving by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    var toast by mutableStateOf<String?>(null)

    private val me get() = Spawn.userId

    init { load() }

    fun load() {
        if (me.isEmpty()) return
        viewModelScope.launch {
            val s = async { runCatching { Spawn.api.stats(me).bodyOrThrow() } }
            val i = async { runCatching { Spawn.api.interests(me).bodyOrThrow() } }
            val so = async { runCatching { Spawn.api.socialMedia(me).bodyOrThrow() } }
            s.await().onSuccess { stats = it }
            i.await().onSuccess { interests = it }
            so.await().onSuccess { socials = it }
        }
        loadCalendar()
    }

    fun changeMonth(delta: Long) {
        month = month.plusMonths(delta)
        loadCalendar()
    }

    private fun loadCalendar() {
        val m = month
        viewModelScope.launch {
            runCatching { Spawn.api.calendar(me, m.monthValue, m.year, java.util.TimeZone.getDefault().id).bodyOrThrow() }
                .onSuccess { if (m == month) calendar = it }
        }
    }

    /** Opens the activity behind a calendar square. */
    fun openCalendarActivity(item: CalendarActivityDTO, onLoaded: (FullFeedActivityDTO) -> Unit) {
        val id = item.activityId ?: return
        viewModelScope.launch {
            runCatching { Spawn.api.activity(id, me).bodyOrThrow() }
                .onSuccess(onLoaded)
                .onFailure { error = it.friendlyMessage() }
        }
    }

    // region Edit profile
    fun save(
        context: Context,
        name: String,
        username: String,
        bio: String,
        newInterests: List<String>,
        instagram: String,
        whatsapp: String,
        photo: Uri?,
        onDone: () -> Unit,
    ) {
        if (saving) return
        val user = Spawn.currentUser ?: return
        saving = true
        error = null
        viewModelScope.launch {
            try {
                var updated = user
                val cleanUsername = username.trim().removePrefix("@")
                if (name.trim() != (user.name ?: "") || cleanUsername != (user.username ?: "") || bio.trim() != (user.bio ?: "")) {
                    updated = Spawn.api.updateUser(me, UserUpdateDTO(bio = bio.trim(), username = cleanUsername, name = name.trim())).bodyOrThrow()
                }
                if (newInterests != interests) {
                    Spawn.api.setInterests(me, newInterests).bodyOrThrow()
                    interests = newInterests
                }
                val ig = instagram.trim().removePrefix("@")
                val wa = whatsapp.trim()
                if (ig != (socials?.instagramHandle ?: "") || wa != (socials?.whatsappDisplay ?: "")) {
                    socials = Spawn.api.updateSocialMedia(me, UpdateUserSocialMediaDTO(whatsappNumber = wa.ifEmpty { null }, instagramUsername = ig.ifEmpty { null })).bodyOrThrow()
                }
                if (photo != null) {
                    val bytes = withContext(Dispatchers.IO) { compress(context, photo) }
                    updated = Spawn.api.updateProfilePicture(me, bytes.toRequestBody("image/jpeg".toMediaType())).bodyOrThrow()
                }
                Spawn.updateUser(updated.copy(relationshipStatus = null))
                toast = "Profile updated"
                onDone()
            } catch (e: Exception) {
                error = e.friendlyMessage()
            } finally {
                saving = false
            }
        }
    }

    /** Square-ish 1024px JPEG, like iOS's resized upload. */
    private fun compress(context: Context, uri: Uri): ByteArray {
        val src = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) } ?: error("Couldn't read that photo")
        val scale = 1024f / maxOf(src.width, src.height)
        val bmp = if (scale < 1f) Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true) else src
        return ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    }
    // endregion

    // region Settings
    fun loadBlocked() {
        viewModelScope.launch {
            runCatching { Spawn.api.blockedUsers(me).bodyOrThrow() }
                .onSuccess { blocked = it }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    fun unblock(b: BlockedUserDTO) {
        val id = b.blockedId ?: return
        blocked = blocked.filterNot { it.blockedId == id }
        viewModelScope.launch {
            runCatching { Spawn.api.unblockUser(me, id).bodyOrThrow() }
                .onSuccess { toast = "Unblocked ${b.blockedName ?: b.blockedUsername ?: ""}".trim(); Spawn.feedChanged() }
                .onFailure { error = it.friendlyMessage(); loadBlocked() }
        }
    }

    fun sendFeedback(type: FeedbackType, message: String, onDone: () -> Unit) {
        if (message.isBlank()) return
        viewModelScope.launch {
            runCatching { Spawn.api.feedback(FeedbackSubmissionDTO(type, me, message.trim())).bodyOrThrow() }
                .onSuccess { toast = "Thanks for the feedback!"; onDone() }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            runCatching { Spawn.api.deleteUser(me).bodyOrThrow() }
                .onSuccess { Spawn.signOutLocally() }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    fun logOut() = Spawn.signOutLocally()
    // endregion
}
