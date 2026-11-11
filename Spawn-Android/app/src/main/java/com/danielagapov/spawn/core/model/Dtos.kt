package com.danielagapov.spawn.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Wire models for the Spawn backend (Spawn-App-Back-End, /api/v1).
 * Field names mirror the backend DTOs and the iOS app's Codable models 1:1.
 * Dates travel as ISO-8601 strings; see core/util/Dates.kt for parsing.
 */

@Serializable
data class BaseUserDTO(
    val id: String,
    val username: String? = null,
    val profilePicture: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val email: String? = null,
    val hasCompletedOnboarding: Boolean? = null,
    val provider: String? = null,
    val relationshipStatus: UserRelationshipType? = null,
    val pendingFriendRequestId: String? = null,
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: username ?: "User"
    val firstName: String get() = name?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: username ?: "there"
    val handle: String get() = "@" + (username ?: "")
}

@Serializable
enum class UserRelationshipType {
    @SerialName("FRIEND") FRIEND,
    @SerialName("RECOMMENDED_FRIEND") RECOMMENDED_FRIEND,
    @SerialName("INCOMING_FRIEND_REQUEST") INCOMING_FRIEND_REQUEST,
    @SerialName("OUTGOING_FRIEND_REQUEST") OUTGOING_FRIEND_REQUEST,
}

@Serializable
enum class UserStatus { EMAIL_VERIFIED, USERNAME_AND_PHONE_NUMBER, NAME_AND_PHOTO, CONTACT_IMPORT, ACTIVE }

@Serializable
data class AuthResponseDTO(
    val user: BaseUserDTO,
    val status: UserStatus? = null,
    val isOAuthUser: Boolean? = null,
)

@Serializable
data class LoginDTO(val usernameOrEmail: String, val password: String)

@Serializable
data class OAuthRegistrationDTO(
    val idToken: String,
    val provider: String,
    val email: String? = null,
    val name: String? = null,
    val profilePictureUrl: String? = null,
)

@Serializable
data class EmailVerificationSendDTO(val email: String)

@Serializable
data class EmailVerificationVerifyDTO(val email: String, val verificationCode: String)

@Serializable
data class EmailVerificationResponseDTO(
    val secondsUntilNextAttempt: Long? = null,
    val message: String? = null,
)

@Serializable
data class UpdateUserDetailsDTO(
    val id: String,
    val username: String,
    val phoneNumber: String,
    val password: String? = null,
)

@Serializable
data class OptionalDetailsDTO(val name: String)

@Serializable
data class UserUpdateDTO(
    val bio: String? = null,
    val username: String? = null,
    val name: String? = null,
)

@Serializable
data class LocationDTO(
    val id: String? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
enum class ParticipationStatus {
    @SerialName("participating") PARTICIPATING,
    @SerialName("invited") INVITED,
    @SerialName("notInvited") NOT_INVITED,
}

@Serializable
data class FullFeedActivityDTO(
    val id: String,
    val title: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: LocationDTO? = null,
    val activityTypeId: String? = null,
    val note: String? = null,
    val icon: String? = null,
    val participantLimit: Int? = null,
    val creatorUser: BaseUserDTO,
    val participantUsers: List<BaseUserDTO>? = null,
    val invitedUsers: List<BaseUserDTO>? = null,
    val chatMessages: List<FullActivityChatMessageDTO>? = null,
    val participationStatus: ParticipationStatus? = null,
    val isSelfOwned: Boolean? = null,
    val isExpired: Boolean? = null,
    val createdAt: String? = null,
    val clientTimezone: String? = null,
)

/** Body for POST /activities and PUT /activities/{id}. */
@Serializable
data class ActivityDTO(
    val id: String? = null,
    val title: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: LocationDTO? = null,
    val activityTypeId: String? = null,
    val note: String? = null,
    val icon: String? = null,
    val participantLimit: Int? = null,
    val creatorUserId: String,
    val participantUserIds: List<String> = emptyList(),
    val invitedUserIds: List<String> = emptyList(),
    val chatMessageIds: List<String> = emptyList(),
    val createdAt: String? = null,
    val clientTimezone: String? = null,
)

@Serializable
data class ActivityCreationResponseDTO(
    val activity: FullFeedActivityDTO,
)

@Serializable
data class ActivityPartialUpdateDTO(
    val title: String? = null,
    val icon: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val participantLimit: Int? = null,
    val note: String? = null,
)

@Serializable
data class MinimalFriendDTO(
    val id: String,
    val username: String? = null,
    val name: String? = null,
    val profilePicture: String? = null,
)

@Serializable
data class ActivityTypeDTO(
    val id: String,
    val title: String,
    val associatedFriends: List<MinimalFriendDTO> = emptyList(),
    val icon: String = "⭐️",
    val orderNum: Int = 0,
    val ownerUserId: String? = null,
    val isPinned: Boolean = false,
)

@Serializable
data class BatchActivityTypeUpdateDTO(
    val updatedActivityTypes: List<ActivityTypeDTO>,
    val deletedActivityTypeIds: List<String>,
)

@Serializable
data class FullActivityChatMessageDTO(
    val id: String,
    val content: String,
    val timestamp: String? = null,
    val senderUser: BaseUserDTO,
    val activityId: String,
    val likedByUsers: List<BaseUserDTO>? = null,
)

@Serializable
data class CreateChatMessageDTO(
    val content: String,
    val senderUserId: String,
    val activityId: String,
)

@Serializable
data class ChatMessageLikesDTO(val chatMessageId: String? = null, val userId: String? = null)

@Serializable
data class FetchFriendRequestDTO(
    val id: String,
    val senderUser: BaseUserDTO,
    val mutualFriendCount: Int? = null,
)

@Serializable
data class FetchSentFriendRequestDTO(
    val id: String,
    val receiverUser: BaseUserDTO,
)

@Serializable
data class CreateFriendRequestDTO(
    val id: String? = null,
    val senderUserId: String,
    val receiverUserId: String,
)

@Serializable
data class FullFriendUserDTO(
    val id: String,
    val username: String? = null,
    val profilePicture: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val email: String? = null,
) {
    fun toBase() = BaseUserDTO(id = id, username = username, profilePicture = profilePicture, name = name, bio = bio, email = email)
}

@Serializable
data class RecommendedFriendUserDTO(
    val id: String,
    val username: String? = null,
    val profilePicture: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val email: String? = null,
    val mutualFriendCount: Int? = null,
    val relationshipStatus: UserRelationshipType? = null,
    val pendingFriendRequestId: String? = null,
) {
    fun toBase() = BaseUserDTO(
        id = id, username = username, profilePicture = profilePicture, name = name, bio = bio, email = email,
        relationshipStatus = relationshipStatus, pendingFriendRequestId = pendingFriendRequestId,
    )
}

@Serializable
data class SearchResultUserDTO(
    val user: BaseUserDTO,
    val relationshipType: UserRelationshipType,
    val mutualFriendCount: Int? = null,
    val friendRequestId: String? = null,
)

@Serializable
data class SearchedUserResult(val users: List<SearchResultUserDTO> = emptyList())

@Serializable
data class RecentlySpawnedUserDTO(val user: BaseUserDTO, val dateTime: String? = null)

@Serializable
data class UserStatsDTO(
    val peopleMet: Int = 0,
    val spawnsMade: Int = 0,
    val spawnsJoined: Int = 0,
)

@Serializable
data class CalendarActivityDTO(
    val id: String,
    val date: String,
    val title: String? = null,
    val icon: String? = null,
    val colorHexCode: String? = null,
    val activityId: String? = null,
)

@Serializable
data class UserSocialMediaDTO(
    val id: String? = null,
    val userId: String? = null,
    val whatsappLink: String? = null,
    val instagramLink: String? = null,
    val whatsappNumber: String? = null,
    val instagramUsername: String? = null,
) {
    /** The backend only returns links (https://www.instagram.com/{name}, https://wa.me/{digits}), so read the values back out of them. */
    val instagramHandle: String
        get() = instagramUsername?.takeIf { it.isNotBlank() }
            ?: instagramLink?.substringAfter("instagram.com/", "")?.trim('/')?.takeIf { it.isNotBlank() } ?: ""
    val whatsappDisplay: String
        get() = whatsappNumber?.takeIf { it.isNotBlank() }
            ?: whatsappLink?.substringAfter("wa.me/", "")?.takeIf { it.isNotBlank() }?.let { "+$it" } ?: ""
}

@Serializable
data class UpdateUserSocialMediaDTO(val whatsappNumber: String? = null, val instagramUsername: String? = null)

@Serializable
data class BlockedUserCreationDTO(val blockerId: String, val blockedId: String, val reason: String)

@Serializable
data class BlockedUserDTO(
    val id: String? = null,
    val blockerId: String? = null,
    val blockedId: String? = null,
    val blockerUsername: String? = null,
    val blockedUsername: String? = null,
    val blockedName: String? = null,
    val blockedProfilePicture: String? = null,
    val reason: String? = null,
)

@Serializable
enum class ReportType { HARASSMENT, VIOLENCE, NUDITY, BULLYING }

@Serializable
enum class EntityType { ChatMessage, Activity, User }

@Serializable
data class CreateReportedContentDTO(
    val reporterUserId: String,
    val contentId: String,
    val contentType: EntityType,
    val reportType: ReportType,
    val description: String,
)

@Serializable
enum class FeedbackType { BUG, FEATURE_REQUEST, GENERAL_FEEDBACK }

@Serializable
data class FeedbackSubmissionDTO(
    val type: FeedbackType,
    val fromUserId: String? = null,
    val message: String,
)

@Serializable
data class DeviceTokenDTO(val token: String, val deviceType: String = "ANDROID", val userId: String? = null)

@Serializable
data class ShareCodeDTO(val shareCode: String? = null)

@Serializable
data class IdDTO(val id: String)
