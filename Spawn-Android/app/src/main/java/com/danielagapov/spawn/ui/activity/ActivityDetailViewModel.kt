package com.danielagapov.spawn.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** One open activity: participation, chat, and the owner/safety actions (iOS ActivityInfoViewModel + ChatViewModel). */
class ActivityDetailViewModel : ViewModel() {
    var activity by mutableStateOf<FullFeedActivityDTO?>(null)
        private set
    var chats by mutableStateOf<List<FullActivityChatMessageDTO>>(emptyList())
        private set
    var toggling by mutableStateOf(false)
        private set
    var sending by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    private var pollingId: String? = null

    val me get() = Spawn.userId
    val isOwner get() = activity?.creatorUser?.id == me
    val isGoing get() = isOwner || activity?.participationStatus == ParticipationStatus.PARTICIPATING ||
        activity?.participantUsers.orEmpty().any { it.id == me }
    val isFull get() = activity?.participantLimit?.let { lim -> activity?.participantUsers.orEmpty().size >= lim } == true

    fun open(a: FullFeedActivityDTO) {
        activity = a
        chats = a.chatMessages.orEmpty().sortedBy { it.timestamp }
        viewModelScope.launch {
            runCatching { Spawn.api.activity(a.id, me).bodyOrThrow() }.onSuccess { activity = it }
            loadChats()
        }
    }

    fun loadChats() {
        val id = activity?.id ?: return
        viewModelScope.launch {
            runCatching { Spawn.api.chats(id).bodyOrThrow() }.onSuccess { chats = it.sortedBy { m -> m.timestamp } }
        }
    }

    /** Poll while the chatroom is open (the backend has no websocket; iOS refreshes the same way). */
    fun startChatPolling() {
        val id = activity?.id ?: return
        if (pollingId == id) return
        pollingId = id
        viewModelScope.launch {
            while (isActive && pollingId == id) {
                delay(5_000)
                if (pollingId == id) loadChats()
            }
        }
    }

    fun stopChatPolling() { pollingId = null }

    fun toggleParticipation() {
        val a = activity ?: return
        if (isOwner || toggling) return
        if (!isGoing && isFull) {
            message = "This activity is full."
            return
        }
        toggling = true
        viewModelScope.launch {
            try {
                val updated = Spawn.api.toggleParticipation(a.id, me).bodyOrThrow()
                activity = updated
                Spawn.feedChanged()
            } catch (e: Exception) {
                message = e.friendlyMessage()
            } finally { toggling = false }
        }
    }

    fun send(text: String) {
        val a = activity ?: return
        val body = text.trim()
        if (body.isEmpty() || sending) return
        sending = true
        viewModelScope.launch {
            try {
                val m = Spawn.api.sendChat(CreateChatMessageDTO(body, me, a.id)).bodyOrThrow()
                chats = (chats + m).distinctBy { it.id }
            } catch (e: Exception) {
                message = e.friendlyMessage()
            } finally { sending = false }
        }
    }

    fun toggleLike(m: FullActivityChatMessageDTO) {
        val liked = m.likedByUsers.orEmpty().any { it.id == me }
        val meUser = Spawn.currentUser ?: return
        // optimistic
        chats = chats.map {
            if (it.id != m.id) it else it.copy(
                likedByUsers = if (liked) it.likedByUsers.orEmpty().filterNot { u -> u.id == me } else it.likedByUsers.orEmpty() + meUser
            )
        }
        viewModelScope.launch {
            runCatching {
                if (liked) Spawn.api.unlikeChat(m.id, me).bodyOrThrow() else Spawn.api.likeChat(m.id, me).bodyOrThrow()
            }.onFailure { loadChats() }
        }
    }

    fun deleteMessage(m: FullActivityChatMessageDTO) {
        chats = chats.filterNot { it.id == m.id }
        viewModelScope.launch { runCatching { Spawn.api.deleteChat(m.id).bodyOrThrow() }.onFailure { message = it.friendlyMessage(); loadChats() } }
    }

    fun report(contentId: String, type: EntityType, reportType: ReportType, description: String) {
        viewModelScope.launch {
            try {
                Spawn.api.report(CreateReportedContentDTO(me, contentId, type, reportType, description)).bodyOrThrow()
                message = "Thanks — we’ll review this report."
            } catch (e: Exception) { message = e.friendlyMessage() }
        }
    }

    fun block(user: BaseUserDTO, reason: String = "Blocked from activity") {
        viewModelScope.launch {
            try {
                Spawn.api.blockUser(BlockedUserCreationDTO(me, user.id, reason)).bodyOrThrow()
                chats = chats.filterNot { it.senderUser.id == user.id }
                message = "${user.displayName} is blocked."
                Spawn.feedChanged()
            } catch (e: Exception) { message = e.friendlyMessage() }
        }
    }

    fun delete(onDone: () -> Unit) {
        val a = activity ?: return
        viewModelScope.launch {
            try {
                Spawn.api.deleteActivity(a.id).bodyOrThrow()
                Spawn.feedChanged()
                onDone()
            } catch (e: Exception) { message = e.friendlyMessage() }
        }
    }

    fun update(title: String?, note: String?, onDone: () -> Unit) {
        val a = activity ?: return
        viewModelScope.launch {
            try {
                activity = Spawn.api.updateActivity(a.id, ActivityPartialUpdateDTO(title = title, note = note)).bodyOrThrow()
                Spawn.feedChanged()
                onDone()
            } catch (e: Exception) { message = e.friendlyMessage() }
        }
    }
}
