package com.danielagapov.spawn.ui.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.Dates
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

enum class CreateStep { Type, Time, Location, Confirm, Success }

enum class Duration(val label: String, val minutes: Long?) {
    Indefinite("Indefinite", null), TwoHours("2 hours", 120), OneHour("1 hour", 60), ThirtyMinutes("30 mins", 30)
}

/** Activity creation + activity type management (iOS ActivityCreationViewModel + ActivityTypeViewModel). */
class CreateActivityViewModel : ViewModel() {
    var step by mutableStateOf(CreateStep.Type)
    var types by mutableStateOf<List<ActivityTypeDTO>>(emptyList())
        private set
    var friends by mutableStateOf<List<MinimalFriendDTO>>(emptyList())
        private set
    var selectedType by mutableStateOf<ActivityTypeDTO?>(null)
        private set

    // Time step
    var dayOffset by mutableStateOf(0)   // 0 = today, 1 = tomorrow
    var hour12 by mutableStateOf(7)
    var minute by mutableStateOf(0)
    var pm by mutableStateOf(true)
    var title by mutableStateOf("")
    var duration by mutableStateOf(Duration.Indefinite)

    // Location step
    var location by mutableStateOf<LocationDTO?>(null)

    // Confirm step
    var participantLimit by mutableStateOf<Int?>(null)
    var invited by mutableStateOf<Set<String>>(emptySet())
    var note by mutableStateOf("")

    var submitting by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    var created by mutableStateOf<FullFeedActivityDTO?>(null)
        private set

    init {
        resetTime()
        load()
    }

    fun load() {
        val uid = Spawn.userId.ifEmpty { return }
        viewModelScope.launch {
            runCatching { Spawn.api.activityTypes(uid).bodyOrThrow() }.onSuccess { types = it.sortedWith(compareBy({ !it.isPinned }, { it.orderNum })) }
            runCatching { Spawn.api.minimalFriends(uid).bodyOrThrow() }.onSuccess { friends = it }
        }
    }

    private fun resetTime() {
        // Next quarter hour, like iOS's default picker position.
        val now = LocalDateTime.now().plusMinutes(15)
        val rounded = now.withMinute((now.minute / 15) * 15)
        dayOffset = if (rounded.toLocalDate().isAfter(LocalDate.now())) 1 else 0
        val h = rounded.hour
        pm = h >= 12
        hour12 = if (h % 12 == 0) 12 else h % 12
        minute = rounded.minute
    }

    fun reset() {
        step = CreateStep.Type
        selectedType = null
        title = ""; note = ""
        duration = Duration.Indefinite
        location = null
        participantLimit = null
        invited = emptySet()
        created = null
        error = null
        resetTime()
        load()
    }

    /** Pick a type (from the grid or the home quick-create buttons) and go to the time step. */
    fun pickType(t: ActivityTypeDTO?) {
        selectedType = t
        if (title.isBlank() && t != null) title = ""
        val ids = t?.associatedFriends?.map { it.id }.orEmpty()
        invited = if (ids.isNotEmpty()) ids.toSet() else friends.map { it.id }.toSet()
        if (invited.isEmpty() && friends.isEmpty()) {
            // friends may still be loading; fill in once they arrive
            viewModelScope.launch {
                runCatching { Spawn.api.minimalFriends(Spawn.userId).bodyOrThrow() }.onSuccess {
                    friends = it
                    if (invited.isEmpty()) invited = (ids.ifEmpty { it.map { f -> f.id } }).toSet()
                }
            }
        }
        step = CreateStep.Time
    }

    val startLocal: LocalDateTime
        get() {
            val h24 = (hour12 % 12) + if (pm) 12 else 0
            return LocalDate.now().plusDays(dayOffset.toLong()).atTime(LocalTime.of(h24, minute))
        }

    val effectiveTitle: String get() = title.trim().ifEmpty { selectedType?.title ?: "Hangout" }

    fun timeIsValid(): Boolean = startLocal.isAfter(LocalDateTime.now().minusMinutes(5))

    fun submit() {
        if (submitting) return
        val loc = location ?: run { error = "Pick a location first."; return }
        submitting = true
        error = null
        viewModelScope.launch {
            try {
                val zone = ZoneId.systemDefault()
                val start = startLocal.atZone(zone).toInstant()
                val end = duration.minutes?.let { start.plusSeconds(it * 60) }
                val body = ActivityDTO(
                    id = UUID.randomUUID().toString(),
                    title = effectiveTitle,
                    startTime = Dates.iso(start),
                    endTime = end?.let { Dates.iso(it) },
                    location = loc.copy(id = null),
                    activityTypeId = selectedType?.id,
                    note = note.trim().ifEmpty { null },
                    icon = selectedType?.icon ?: "⭐️",
                    participantLimit = participantLimit,
                    creatorUserId = Spawn.userId,
                    invitedUserIds = invited.toList(),
                    clientTimezone = zone.id,
                )
                created = Spawn.api.createActivity(body).bodyOrThrow().activity
                Spawn.feedChanged()
                step = CreateStep.Success
            } catch (e: Exception) {
                error = e.friendlyMessage()
            } finally { submitting = false }
        }
    }

    // region Activity type management (pin / delete / create), all through PUT /users/{id}/activity-types
    private fun batch(updated: List<ActivityTypeDTO>, deleted: List<String>) {
        viewModelScope.launch {
            try {
                types = Spawn.api.updateActivityTypes(Spawn.userId, BatchActivityTypeUpdateDTO(updated, deleted)).bodyOrThrow()
                    .sortedWith(compareBy({ !it.isPinned }, { it.orderNum }))
                Spawn.feedChanged()
            } catch (e: Exception) {
                error = e.friendlyMessage()
                load()
            }
        }
    }

    fun togglePin(t: ActivityTypeDTO) {
        val u = t.copy(isPinned = !t.isPinned)
        types = types.map { if (it.id == t.id) u else it }.sortedWith(compareBy({ !it.isPinned }, { it.orderNum }))
        batch(listOf(u), emptyList())
    }

    fun deleteType(t: ActivityTypeDTO) {
        types = types.filterNot { it.id == t.id }
        batch(emptyList(), listOf(t.id))
    }

    fun createType(title: String, icon: String, friendIds: Set<String>) {
        val t = ActivityTypeDTO(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            icon = icon.ifBlank { "⭐️" },
            orderNum = (types.maxOfOrNull { it.orderNum } ?: -1) + 1,
            ownerUserId = Spawn.userId,
            associatedFriends = friends.filter { it.id in friendIds },
            isPinned = false,
        )
        types = types + t
        batch(listOf(t), emptyList())
    }

    fun updateType(t: ActivityTypeDTO, title: String, icon: String, friendIds: Set<String>) {
        val u = t.copy(title = title.trim(), icon = icon.ifBlank { t.icon }, associatedFriends = friends.filter { it.id in friendIds })
        types = types.map { if (it.id == t.id) u else it }
        batch(listOf(u), emptyList())
    }
    // endregion
}
