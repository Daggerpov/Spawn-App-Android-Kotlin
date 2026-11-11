package com.danielagapov.spawn.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.UUID

/** Another user's profile (iOS ProfileView for a non-self user + ProfileViewModel friendship actions). */
class UserProfileViewModel : ViewModel() {
    var user by mutableStateOf<BaseUserDTO?>(null); private set
    var interests by mutableStateOf<List<String>>(emptyList()); private set
    var socials by mutableStateOf<UserSocialMediaDTO?>(null); private set
    var activities by mutableStateOf<List<FullFeedActivityDTO>>(emptyList()); private set
    var types by mutableStateOf<List<ActivityTypeDTO>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    var toast by mutableStateOf<String?>(null)
    /** Shows the Figma "Success! You've added X as a friend" sheet. */
    var justBecameFriends by mutableStateOf(false)
    /** Set once the user is blocked, so the screen can close itself. */
    var blocked by mutableStateOf(false); private set

    private val me get() = Spawn.userId

    val relationship: UserRelationshipType? get() = user?.relationshipStatus
    val isFriend get() = relationship == UserRelationshipType.FRIEND

    fun load(target: BaseUserDTO) {
        if (user?.id != target.id) {
            user = target
            interests = emptyList(); socials = null; activities = emptyList()
        }
        viewModelScope.launch {
            loading = true
            val u = async { runCatching { Spawn.api.getUser(target.id, me).bodyOrThrow() } }
            val i = async { runCatching { Spawn.api.interests(target.id).bodyOrThrow() } }
            val s = async { runCatching { Spawn.api.socialMedia(target.id).bodyOrThrow() } }
            val a = async { runCatching { Spawn.api.profileActivities(target.id, me).bodyOrThrow() } }
            u.await().onSuccess { user = it }.onFailure { error = it.friendlyMessage() }
            i.await().onSuccess { interests = it }
            s.await().onSuccess { socials = it }
            a.await().onSuccess { list -> activities = list.filter { it.isExpired != true }.sortedBy { it.startTime ?: "" } }
            loading = false
        }
    }

    private fun refresh() { user?.let { load(it) } }

    fun addFriend() = act {
        Spawn.api.sendFriendRequest(CreateFriendRequestDTO(id = UUID.randomUUID().toString(), senderUserId = me, receiverUserId = user!!.id)).bodyOrThrow()
        user = user?.copy(relationshipStatus = UserRelationshipType.OUTGOING_FRIEND_REQUEST)
        toast = "Friend request sent"
    }

    fun accept() = act {
        val reqId = user?.pendingFriendRequestId ?: incomingRequestId() ?: error("Couldn't find this friend request")
        Spawn.api.friendRequestAction(reqId, "accept").bodyOrThrow()
        user = user?.copy(relationshipStatus = UserRelationshipType.FRIEND, pendingFriendRequestId = null)
        justBecameFriends = true
        Spawn.feedChanged()
        refresh()
    }

    fun deny() = act {
        val reqId = user?.pendingFriendRequestId ?: incomingRequestId() ?: error("Couldn't find this friend request")
        Spawn.api.friendRequestAction(reqId, "reject").bodyOrThrow()
        user = user?.copy(relationshipStatus = UserRelationshipType.RECOMMENDED_FRIEND, pendingFriendRequestId = null)
    }

    fun cancelRequest() = act {
        val reqId = user?.pendingFriendRequestId
            ?: Spawn.api.sentFriendRequests(me).bodyOrThrow().firstOrNull { it.receiverUser.id == user?.id }?.id
            ?: error("Couldn't find this friend request")
        Spawn.api.friendRequestAction(reqId, "cancel").bodyOrThrow()
        user = user?.copy(relationshipStatus = UserRelationshipType.RECOMMENDED_FRIEND, pendingFriendRequestId = null)
    }

    fun removeFriend() = act {
        Spawn.api.removeFriend(me, user!!.id).bodyOrThrow()
        user = user?.copy(relationshipStatus = UserRelationshipType.RECOMMENDED_FRIEND)
        activities = emptyList()
        toast = "Removed from friends"
        Spawn.feedChanged()
    }

    fun block(reason: String) = act {
        Spawn.api.blockUser(BlockedUserCreationDTO(me, user!!.id, reason)).bodyOrThrow()
        toast = "${user?.firstName} has been blocked"
        Spawn.feedChanged()
        blocked = true
    }

    fun report(type: ReportType, description: String) = act {
        Spawn.api.report(CreateReportedContentDTO(me, user!!.id, EntityType.User, type, description)).bodyOrThrow()
        toast = "Thanks — we'll review this report"
    }

    // region Add to Activity Type (Figma "Add Friend to Tag")
    fun loadTypes() {
        viewModelScope.launch {
            runCatching { Spawn.api.activityTypes(me).bodyOrThrow() }
                .onSuccess { types = it.sortedWith(compareBy({ !it.isPinned }, { it.orderNum })) }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    fun typesContainingUser(): Set<String> = types.filter { t -> t.associatedFriends.any { it.id == user?.id } }.map { it.id }.toSet()

    fun saveTypes(selected: Set<String>, onDone: () -> Unit) {
        val u = user ?: return
        val minimal = MinimalFriendDTO(u.id, u.username, u.name, u.profilePicture)
        val changed = types.mapNotNull { t ->
            val has = t.associatedFriends.any { it.id == u.id }
            val want = t.id in selected
            when {
                want && !has -> t.copy(associatedFriends = t.associatedFriends + minimal)
                !want && has -> t.copy(associatedFriends = t.associatedFriends.filterNot { it.id == u.id })
                else -> null
            }
        }
        if (changed.isEmpty()) { onDone(); return }
        act {
            types = Spawn.api.updateActivityTypes(me, BatchActivityTypeUpdateDTO(changed, emptyList())).bodyOrThrow()
            toast = "Saved"
            onDone()
        }
    }
    // endregion

    private suspend fun incomingRequestId(): String? =
        Spawn.api.incomingFriendRequests(me).bodyOrThrow().firstOrNull { it.senderUser.id == user?.id }?.id

    private fun act(block: suspend () -> Unit) {
        if (busy || user == null) return
        busy = true
        viewModelScope.launch {
            try { block() } catch (e: Exception) { error = e.friendlyMessage() } finally { busy = false }
        }
    }
}
