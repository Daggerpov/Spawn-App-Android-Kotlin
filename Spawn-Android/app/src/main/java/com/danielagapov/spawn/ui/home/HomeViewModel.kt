package com.danielagapov.spawn.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.ActivityTypeDTO
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.ActivityStatus
import com.danielagapov.spawn.core.util.Dates
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Feed + activity types for the home tab (iOS FeedViewModel). */
class HomeViewModel : ViewModel() {
    var activities by mutableStateOf<List<FullFeedActivityDTO>>(emptyList())
        private set
    var activityTypes by mutableStateOf<List<ActivityTypeDTO>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    /** Ticks every 30s so "In 5 min"/"Happening Now" stay current. */
    var clock by mutableStateOf(0)
        private set

    init {
        viewModelScope.launch { Spawn.feedVersion.collect { load(silent = activities.isNotEmpty()) } }
        viewModelScope.launch { while (true) { delay(30_000); clock++ } }
    }

    fun refresh() {
        refreshing = true
        load(silent = true)
    }

    fun load(silent: Boolean = false) {
        val uid = Spawn.userId.ifEmpty { return }
        if (!silent) loading = true
        viewModelScope.launch {
            try {
                val feed = async { Spawn.api.feedActivities(uid).bodyOrThrow() }
                val types = async { runCatching { Spawn.api.activityTypes(uid).bodyOrThrow() }.getOrNull() }
                activities = sortFeed(feed.await())
                types.await()?.let { activityTypes = sortTypes(it) }
                error = null
            } catch (e: Exception) {
                error = e.friendlyMessage()
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    fun replace(updated: FullFeedActivityDTO) {
        activities = activities.map { if (it.id == updated.id) updated else it }
    }

    fun remove(id: String) {
        activities = activities.filterNot { it.id == id }
    }

    companion object {
        /** Live first, then soonest; drop finished ones (backend already filters most). */
        fun sortFeed(list: List<FullFeedActivityDTO>): List<FullFeedActivityDTO> =
            list.filter { ActivityStatus.of(it) != ActivityStatus.Past && it.isExpired != true }
                .sortedWith(compareBy({ !ActivityStatus.of(it).isLive }, { Dates.parse(it.startTime) ?: java.time.Instant.MAX }))

        /** Pinned first, then by the user's order (iOS FeedViewModel.sortedActivityTypes). */
        fun sortTypes(list: List<ActivityTypeDTO>): List<ActivityTypeDTO> =
            list.sortedWith(compareBy({ !it.isPinned }, { it.orderNum }))
    }
}
