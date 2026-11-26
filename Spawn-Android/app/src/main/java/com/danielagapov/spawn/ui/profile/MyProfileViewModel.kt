package com.danielagapov.spawn.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.ApiException
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.push.Push
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.compressPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
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
    // Exclude a deleted ID even if a calendar request started before deletion completes afterward.
    private val deletedActivityIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            Spawn.deletedActivities.collect { id ->
                deletedActivityIds.add(id)
                calendar = withoutDeletedActivities(calendar)
                calendarMonths = calendarMonths.map { (month, items) -> month to withoutDeletedActivities(items) }
                dayActivities = dayActivities.filterNot { it.id in deletedActivityIds }
            }
        }
        load()
    }

    private fun withoutDeletedActivities(items: List<CalendarActivityDTO>) =
        items.filterNot { it.activityId in deletedActivityIds }

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
                .onSuccess { if (m == month) calendar = withoutDeletedActivities(it) }
        }
    }

    // Full "Your Event Calendar" page (Figma 1321:11975): six months back through three ahead.
    var calendarMonths by mutableStateOf<List<Pair<YearMonth, List<CalendarActivityDTO>>>>(emptyList()); private set

    fun loadCalendarMonths() {
        val now = YearMonth.now()
        val tz = java.util.TimeZone.getDefault().id
        viewModelScope.launch {
            calendarMonths = (-6L..3L).map { now.plusMonths(it) }.map { m ->
                async { m to (runCatching { Spawn.api.calendar(me, m.monthValue, m.year, tz).bodyOrThrow() }.getOrNull() ?: emptyList()) }
            }.awaitAll().map { (month, items) -> month to withoutDeletedActivities(items) }
        }
    }

    // "Events - <day>" page (Figma 1557:3742) for a calendar day with more than one activity.
    var dayDate by mutableStateOf<LocalDate?>(null); private set
    var dayActivities by mutableStateOf<List<FullFeedActivityDTO>>(emptyList()); private set
    var dayLoading by mutableStateOf(false); private set

    fun openDay(date: LocalDate, items: List<CalendarActivityDTO>) {
        dayDate = date
        dayActivities = emptyList()
        dayLoading = true
        viewModelScope.launch {
            dayActivities = items.mapNotNull { it.activityId }.distinct()
                .map { id -> async { runCatching { Spawn.api.activity(id, me).bodyOrThrow() }.getOrNull() } }
                .awaitAll().filterNotNull().filterNot { it.id in deletedActivityIds }
            dayLoading = false
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
                    val bytes = withContext(Dispatchers.IO) { compressPhoto(context, photo) }
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

    var changingPassword by mutableStateOf(false)
        private set

    /** iOS ChangePasswordView. A 401 means the current password is wrong, which the page shows on that field. */
    fun changePassword(current: String, new: String, onWrongCurrent: () -> Unit, onDone: () -> Unit) {
        if (changingPassword) return
        changingPassword = true
        viewModelScope.launch {
            runCatching { Spawn.api.changePassword(PasswordChangeDTO(current, new)).bodyOrThrow() }
                .onSuccess { toast = "Your password has been changed."; onDone() }
                .onFailure { if (it is ApiException && it.code == 401) onWrongCurrent() else error = it.friendlyMessage() }
            changingPassword = false
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            runCatching { Spawn.api.deleteUser(me).bodyOrThrow() }
                .onSuccess { Spawn.signOutLocally() }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            Push.unregister()
            Spawn.signOutLocally()
        }
    }
    // endregion
}
