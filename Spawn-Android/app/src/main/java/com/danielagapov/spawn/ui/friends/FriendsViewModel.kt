package com.danielagapov.spawn.ui.friends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielagapov.spawn.core.model.*
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Friends tab state (iOS FriendsTabViewModel + FriendRequestsViewModel): friends, incoming/sent requests,
 * recently-spawned-with and recommended people, plus user search.
 */
class FriendsViewModel : ViewModel() {
    var friends by mutableStateOf<List<FullFriendUserDTO>>(emptyList()); private set
    var incoming by mutableStateOf<List<FetchFriendRequestDTO>>(emptyList()); private set
    var sent by mutableStateOf<List<FetchSentFriendRequestDTO>>(emptyList()); private set
    var recent by mutableStateOf<List<RecentlySpawnedUserDTO>>(emptyList()); private set
    var recommended by mutableStateOf<List<RecommendedFriendUserDTO>>(emptyList()); private set
    /** People we've just sent a request to this session, so the button flips to "Requested" right away. */
    var requested by mutableStateOf<Set<String>>(emptySet()); private set

    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<SearchResultUserDTO>>(emptyList()); private set
    var searching by mutableStateOf(false); private set

    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    var toast by mutableStateOf<String?>(null)

    private var searchJob: Job? = null
    private val uid get() = Spawn.userId

    init { load() }

    fun load() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            loading = friends.isEmpty()
            val f = async { runCatching { Spawn.api.friends(uid).bodyOrThrow() } }
            val i = async { runCatching { Spawn.api.incomingFriendRequests(uid).bodyOrThrow() } }
            val s = async { runCatching { Spawn.api.sentFriendRequests(uid).bodyOrThrow() } }
            val r = async { runCatching { Spawn.api.recentlySpawnedWith(uid).bodyOrThrow() } }
            val rec = async { runCatching { Spawn.api.recommendedFriends(uid).bodyOrThrow() } }
            f.await().onSuccess { friends = it.distinctBy { u -> u.id }.sortedBy { u -> (u.name ?: u.username ?: "").lowercase() } }
                .onFailure { error = it.friendlyMessage() }
            i.await().onSuccess { incoming = it }
            s.await().onSuccess { sent = it }
            r.await().onSuccess { recent = it }
            rec.await().onSuccess { recommended = it }
            loading = false
        }
    }

    private val friendIds get() = friends.map { it.id }.toSet()
    private val sentIds get() = sent.map { it.receiverUser.id }.toSet() + requested
    private val incomingBySender get() = incoming.associateBy { it.senderUser.id }

    /** Recently-spawned-with people who aren't friends yet (Figma "Recently Spawned With" with Add +). */
    val recentNonFriends: List<BaseUserDTO>
        get() = recent.map { it.user }.filter { it.id !in friendIds && it.id != uid }.distinctBy { it.id }

    val suggestions: List<RecommendedFriendUserDTO>
        get() = recommended.filter { it.id !in friendIds && it.id !in recentNonFriends.map { r -> r.id } }

    fun relationshipOf(userId: String): UserRelationshipType? = when {
        userId in friendIds -> UserRelationshipType.FRIEND
        userId in incomingBySender -> UserRelationshipType.INCOMING_FRIEND_REQUEST
        userId in sentIds -> UserRelationshipType.OUTGOING_FRIEND_REQUEST
        else -> null
    }

    fun incomingRequestFrom(userId: String) = incomingBySender[userId]

    // region Search
    fun onQueryChange(q: String) {
        query = q
        searchJob?.cancel()
        if (q.isBlank()) { results = emptyList(); searching = false; return }
        searchJob = viewModelScope.launch {
            searching = true
            delay(300)
            runCatching { Spawn.api.filteredUsers(uid, q.trim()).bodyOrThrow() }
                .onSuccess { results = it.users.filter { u -> u.user.id != uid }.distinctBy { u -> u.user.id } }
            searching = false
        }
    }

    fun clearSearch() { onQueryChange("") }

    fun friendsMatching(q: String): List<FullFriendUserDTO> {
        val t = q.trim().lowercase()
        if (t.isEmpty()) return friends
        return friends.filter { (it.name ?: "").lowercase().contains(t) || (it.username ?: "").lowercase().contains(t) }
    }
    // endregion

    // region Actions
    fun sendRequest(userId: String) {
        if (userId in requested) return
        requested = requested + userId
        viewModelScope.launch {
            runCatching { Spawn.api.sendFriendRequest(CreateFriendRequestDTO(id = java.util.UUID.randomUUID().toString(), senderUserId = uid, receiverUserId = userId)).bodyOrThrow() }
                .onSuccess { toast = "Friend request sent"; refreshRequests() }
                .onFailure { requested = requested - userId; error = it.friendlyMessage() }
        }
    }

    fun accept(req: FetchFriendRequestDTO) {
        incoming = incoming.filterNot { it.id == req.id }
        viewModelScope.launch {
            runCatching { Spawn.api.friendRequestAction(req.id, "accept").bodyOrThrow() }
                .onSuccess { toast = "You're now friends with ${req.senderUser.firstName}"; Spawn.feedChanged(); load() }
                .onFailure { error = it.friendlyMessage(); load() }
        }
    }

    fun reject(req: FetchFriendRequestDTO) {
        incoming = incoming.filterNot { it.id == req.id }
        viewModelScope.launch {
            runCatching { Spawn.api.friendRequestAction(req.id, "reject").bodyOrThrow() }.onFailure { error = it.friendlyMessage(); load() }
        }
    }

    fun cancel(req: FetchSentFriendRequestDTO) {
        sent = sent.filterNot { it.id == req.id }
        requested = requested - req.receiverUser.id
        viewModelScope.launch {
            runCatching { Spawn.api.friendRequestAction(req.id, "cancel").bodyOrThrow() }.onFailure { error = it.friendlyMessage(); load() }
        }
    }

    fun removeFriend(userId: String) {
        val before = friends
        friends = friends.filterNot { it.id == userId }
        viewModelScope.launch {
            runCatching { Spawn.api.removeFriend(uid, userId).bodyOrThrow() }
                .onSuccess { toast = "Removed from friends"; Spawn.feedChanged() }
                .onFailure { friends = before; error = it.friendlyMessage() }
        }
    }

    fun block(userId: String, reason: String) {
        friends = friends.filterNot { it.id == userId }
        recent = recent.filterNot { it.user.id == userId }
        recommended = recommended.filterNot { it.id == userId }
        viewModelScope.launch {
            runCatching { Spawn.api.blockUser(BlockedUserCreationDTO(uid, userId, reason)).bodyOrThrow() }
                .onSuccess { toast = "User blocked"; Spawn.feedChanged() }
                .onFailure { error = it.friendlyMessage(); load() }
        }
    }

    fun report(userId: String, type: ReportType, description: String) {
        viewModelScope.launch {
            runCatching { Spawn.api.report(CreateReportedContentDTO(uid, userId, EntityType.User, type, description)).bodyOrThrow() }
                .onSuccess { toast = "Thanks — we'll review this report" }
                .onFailure { error = it.friendlyMessage() }
        }
    }

    private suspend fun refreshRequests() {
        runCatching { Spawn.api.sentFriendRequests(uid).bodyOrThrow() }.onSuccess { sent = it }
    }
    // endregion
}
