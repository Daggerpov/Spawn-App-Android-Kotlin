package com.example.spawn_app_android.data.remote.dto

/**
 * DTOs.kt
 * Data Transfer Objects for API communication
 * Converted from iOS Swift DTOs
 */

import com.google.gson.annotations.SerializedName

// ==================== USER DTOs ====================

/**
 * Base user DTO returned from most user-related endpoints
 */
data class BaseUserDTO(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null,
    val phoneNumber: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastLocationUpdate: String? = null
)

/**
 * DTO for creating a new user
 */
data class UserCreateDTO(
    val username: String,
    val name: String,
    val email: String,
    val profilePictureData: String? = null, // Base64 encoded image
    val profilePictureUrl: String? = null
)

/**
 * DTO for updating user information
 */
data class UserUpdateDTO(
    val username: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val phoneNumber: String? = null
)

/**
 * DTO for updating optional user details
 */
data class OptionalDetailsDTO(
    val bio: String? = null,
    val phoneNumber: String? = null
)

/**
 * DTO for updating user location
 */
data class LocationUpdateDTO(
    val latitude: Double,
    val longitude: Double
)

// ==================== AUTH DTOs ====================

/**
 * Sign-in request with email/password
 */
data class SignInRequestDTO(
    val email: String,
    val password: String? = null
)

/**
 * OAuth login request (Google Sign-In)
 */
data class OAuthLoginRequestDTO(
    val email: String,
    val idToken: String,
    val provider: String = "google"
)

/**
 * Quick sign-in request with stored token
 */
data class QuickSignInRequestDTO(
    val email: String,
    val deviceId: String? = null
)

/**
 * OAuth registration request
 */
data class OAuthRegisterRequestDTO(
    val email: String,
    val idToken: String,
    val provider: String = "google",
    val name: String? = null,
    val profilePictureUrl: String? = null
)

/**
 * Verification code send request
 */
data class VerificationSendRequestDTO(
    val email: String
)

/**
 * Verification code check request
 */
data class VerificationCheckRequestDTO(
    val email: String,
    val code: String
)

/**
 * Verification check response
 */
data class VerificationCheckResponseDTO(
    val verified: Boolean,
    val message: String? = null
)

// ==================== FRIEND DTOs ====================

/**
 * Friend request DTO
 */
data class FriendRequestDTO(
    val senderId: String,
    val receiverId: String
)

/**
 * Friend request response DTO
 */
data class FriendRequestResponseDTO(
    val id: String,
    val sender: BaseUserDTO,
    val receiver: BaseUserDTO,
    val status: String, // "pending", "accepted", "declined"
    val createdAt: String
)

// ==================== FRIEND TAG DTOs ====================

/**
 * Friend tag DTO
 */
data class FriendTagDTO(
    val id: String,
    val name: String,
    val color: String,
    val ownerId: String,
    val friends: List<BaseUserDTO>? = null,
    val displayName: String? = null
)

/**
 * Create friend tag DTO
 */
data class FriendTagCreateDTO(
    val name: String,
    val color: String,
    val ownerId: String,
    val friendIds: List<String>? = null
)

/**
 * Update friend tag DTO
 */
data class FriendTagUpdateDTO(
    val name: String? = null,
    val color: String? = null,
    val friendIds: List<String>? = null
)

// ==================== ACTIVITY DTOs ====================

/**
 * Activity DTO
 */
data class ActivityDTO(
    val id: String,
    val title: String,
    val description: String? = null,
    val location: LocationDTO? = null,
    val startTime: String,
    val endTime: String? = null,
    val creatorId: String,
    val creator: BaseUserDTO? = null,
    val activityType: ActivityTypeDTO? = null,
    val participants: List<BaseUserDTO>? = null,
    val invitedUsers: List<BaseUserDTO>? = null,
    val invitedTags: List<FriendTagDTO>? = null,
    val isPublic: Boolean = false,
    val maxParticipants: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val note: String? = null
)

/**
 * Location DTO
 */
data class LocationDTO(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val name: String? = null
)

/**
 * Create activity DTO
 */
data class ActivityCreateDTO(
    val title: String,
    val description: String? = null,
    val location: LocationDTO? = null,
    val startTime: String,
    val endTime: String? = null,
    val creatorId: String,
    val activityTypeId: String? = null,
    val invitedUserIds: List<String>? = null,
    val invitedTagIds: List<String>? = null,
    val isPublic: Boolean = false,
    val maxParticipants: Int? = null,
    val note: String? = null
)

/**
 * Update activity DTO
 */
data class ActivityUpdateDTO(
    val title: String? = null,
    val description: String? = null,
    val location: LocationDTO? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val activityTypeId: String? = null,
    val invitedUserIds: List<String>? = null,
    val invitedTagIds: List<String>? = null,
    val isPublic: Boolean? = null,
    val maxParticipants: Int? = null,
    val note: String? = null
)

/**
 * Activity RSVP DTO
 */
data class ActivityRsvpDTO(
    val userId: String,
    val status: String = "attending" // "attending", "maybe", "not_attending"
)

/**
 * Activity type DTO
 */
data class ActivityTypeDTO(
    val id: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val category: String? = null
)

// ==================== CALENDAR DTOs ====================

/**
 * Calendar activity DTO (simplified view for calendar)
 */
data class CalendarActivityDTO(
    val id: String,
    val title: String,
    val date: String, // YYYY-MM-DD format
    val startTime: String,
    val endTime: String? = null,
    val activityType: ActivityTypeDTO? = null,
    val isOwner: Boolean = false
)

// ==================== NOTIFICATION DTOs ====================

/**
 * Notification DTO
 */
data class NotificationDTO(
    val id: String,
    val type: String, // "friend_request", "activity_invite", "activity_update", etc.
    val title: String,
    val message: String,
    val userId: String,
    val relatedId: String? = null, // ID of related entity (activity, user, etc.)
    val isRead: Boolean = false,
    val createdAt: String
)

/**
 * Device token DTO for push notifications
 */
data class DeviceTokenDTO(
    val userId: String,
    val token: String,
    val platform: String = "android"
)

// ==================== BLOCKED USER DTOs ====================

/**
 * Block user DTO
 */
data class BlockUserDTO(
    val blockerId: String,
    val blockedId: String
)

// ==================== CONTACTS DTOs ====================

/**
 * Contacts cross-reference DTO
 */
data class ContactsCrossReferenceDTO(
    val userId: String,
    val phoneNumbers: List<String>? = null,
    val emails: List<String>? = null
)

// ==================== CACHE DTOs ====================

/**
 * Cache validation request DTO
 */
data class CacheValidationRequestDTO(
    val timestamps: Map<String, String>
)

/**
 * Cache validation response DTO
 */
data class CacheValidationResponseDTO(
    val isValid: Boolean,
    val lastModified: String? = null
)

// ==================== ERROR DTOs ====================

/**
 * Error response from API
 */
data class ErrorResponseDTO(
    val message: String,
    val statusCode: Int? = null,
    val error: String? = null
)

// ==================== EMPTY REQUEST/RESPONSE ====================

/**
 * Empty request body for PUT requests that don't need a body
 */
class EmptyRequestBody

/**
 * Empty response for endpoints that return no content
 */
class EmptyResponse

/**
 * Empty object for generic empty responses
 */
class EmptyObject
