package com.example.spawn_app_android.data.remote


import com.example.spawn_app_android.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * ApiService.kt
 *
 * Created by Ethan Dsouza on 2025-12-06
 *
 * Retrofit API Service interface defining all backend endpoints.
 */
interface ApiService {

    // ==================== AUTH ENDPOINTS ====================

    /**
     * Sign in with credentials
     */
    @POST("auth/sign-in")
    suspend fun signIn(
        @Body signInRequest: SignInRequestDTO
    ): Response<BaseUserDTO>

    /**
     * Login with OAuth (Google Sign-In)
     */
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: OAuthLoginRequestDTO
    ): Response<BaseUserDTO>

    /**
     * Quick sign-in (auto-login with stored credentials)
     */
    @POST("auth/quick-sign-in")
    suspend fun quickSignIn(
        @Body quickSignInRequest: QuickSignInRequestDTO
    ): Response<BaseUserDTO>

    /**
     * Register with OAuth
     */
    @POST("auth/register/oauth")
    suspend fun registerWithOAuth(
        @Body oAuthRequest: OAuthRegisterRequestDTO
    ): Response<BaseUserDTO>

    /**
     * Send verification code
     */
    @POST("auth/register/verification/send")
    suspend fun sendVerificationCode(
        @Body request: VerificationSendRequestDTO
    ): Response<Unit>

    /**
     * Check verification code
     */
    @POST("auth/register/verification/check")
    suspend fun checkVerificationCode(
        @Body request: VerificationCheckRequestDTO
    ): Response<VerificationCheckResponseDTO>

    /**
     * Create a new user
     */
    @POST("auth/make-user")
    suspend fun createUser(
        @Body userCreateDTO: UserCreateDTO,
        @Query("profilePicUrl") profilePicUrl: String? = null
    ): Response<BaseUserDTO>

    /**
     * Get user details during auth flow
     */
    @GET("auth/user/details")
    suspend fun getAuthUserDetails(
        @Query("email") email: String
    ): Response<BaseUserDTO>

    /**
     * Refresh access token
     */
    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<Unit>

    // ==================== USER ENDPOINTS ====================

    /**
     * Get user by ID
     */
    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String
    ): Response<BaseUserDTO>

    /**
     * Get all users
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<BaseUserDTO>>

    /**
     * Update user
     */
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body userUpdateDTO: UserUpdateDTO
    ): Response<BaseUserDTO>

    /**
     * Delete user
     */
    @DELETE("users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    ): Response<Unit>

    /**
     * Update user profile picture
     */
    @PATCH("users/update-pfp/{userId}")
    suspend fun updateProfilePicture(
        @Path("userId") userId: String,
        @Body imageData: RequestBody
    ): Response<BaseUserDTO>

    /**
     * Update user optional details
     */
    @PATCH("users/optional-details/{userId}")
    suspend fun updateOptionalDetails(
        @Path("userId") userId: String,
        @Body optionalDetails: OptionalDetailsDTO
    ): Response<BaseUserDTO>

    /**
     * Search users
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("excludeUserId") excludeUserId: String? = null
    ): Response<List<BaseUserDTO>>

    // ==================== FRIEND ENDPOINTS ====================

    /**
     * Get friends for a user
     */
    @GET("friends/{userId}")
    suspend fun getFriends(
        @Path("userId") userId: String
    ): Response<List<BaseUserDTO>>

    /**
     * Send friend request
     */
    @POST("friends/request")
    suspend fun sendFriendRequest(
        @Body friendRequestDTO: FriendRequestDTO
    ): Response<FriendRequestResponseDTO>

    /**
     * Accept friend request
     */
    @PUT("friends/request/{requestId}/accept")
    suspend fun acceptFriendRequest(
        @Path("requestId") requestId: String
    ): Response<Unit>

    /**
     * Decline friend request
     */
    @PUT("friends/request/{requestId}/decline")
    suspend fun declineFriendRequest(
        @Path("requestId") requestId: String
    ): Response<Unit>

    /**
     * Get pending friend requests
     */
    @GET("friends/requests/pending/{userId}")
    suspend fun getPendingFriendRequests(
        @Path("userId") userId: String
    ): Response<List<FriendRequestResponseDTO>>

    /**
     * Get sent friend requests
     */
    @GET("friends/requests/sent/{userId}")
    suspend fun getSentFriendRequests(
        @Path("userId") userId: String
    ): Response<List<FriendRequestResponseDTO>>

    /**
     * Remove friend
     */
    @DELETE("friends/{userId}/{friendId}")
    suspend fun removeFriend(
        @Path("userId") userId: String,
        @Path("friendId") friendId: String
    ): Response<Unit>

    // ==================== FRIEND TAG ENDPOINTS ====================

    /**
     * Get friend tags for a user
     */
    @GET("friend-tags/{userId}")
    suspend fun getFriendTags(
        @Path("userId") userId: String
    ): Response<List<FriendTagDTO>>

    /**
     * Create friend tag
     */
    @POST("friend-tags")
    suspend fun createFriendTag(
        @Body friendTagCreateDTO: FriendTagCreateDTO
    ): Response<FriendTagDTO>

    /**
     * Update friend tag
     */
    @PUT("friend-tags/{tagId}")
    suspend fun updateFriendTag(
        @Path("tagId") tagId: String,
        @Body friendTagUpdateDTO: FriendTagUpdateDTO
    ): Response<FriendTagDTO>

    /**
     * Delete friend tag
     */
    @DELETE("friend-tags/{tagId}")
    suspend fun deleteFriendTag(
        @Path("tagId") tagId: String
    ): Response<Unit>

    /**
     * Add friend to tag
     */
    @POST("friend-tags/{tagId}/friends/{friendId}")
    suspend fun addFriendToTag(
        @Path("tagId") tagId: String,
        @Path("friendId") friendId: String
    ): Response<FriendTagDTO>

    /**
     * Remove friend from tag
     */
    @DELETE("friend-tags/{tagId}/friends/{friendId}")
    suspend fun removeFriendFromTag(
        @Path("tagId") tagId: String,
        @Path("friendId") friendId: String
    ): Response<Unit>

    // ==================== ACTIVITY/EVENT ENDPOINTS ====================

    /**
     * Get activities for a user
     */
    @GET("activities/user/{userId}")
    suspend fun getUserActivities(
        @Path("userId") userId: String
    ): Response<List<ActivityDTO>>

    /**
     * Get activity by ID
     */
    @GET("activities/{activityId}")
    suspend fun getActivity(
        @Path("activityId") activityId: String
    ): Response<ActivityDTO>

    /**
     * Create activity
     */
    @POST("activities")
    suspend fun createActivity(
        @Body activityCreateDTO: ActivityCreateDTO
    ): Response<ActivityDTO>

    /**
     * Update activity
     */
    @PUT("activities/{activityId}")
    suspend fun updateActivity(
        @Path("activityId") activityId: String,
        @Body activityUpdateDTO: ActivityUpdateDTO
    ): Response<ActivityDTO>

    /**
     * Delete activity
     */
    @DELETE("activities/{activityId}")
    suspend fun deleteActivity(
        @Path("activityId") activityId: String
    ): Response<Unit>

    /**
     * Get feed activities (activities from friends)
     */
    @GET("activities/feed/{userId}")
    suspend fun getFeedActivities(
        @Path("userId") userId: String
    ): Response<List<ActivityDTO>>

    /**
     * RSVP to activity
     */
    @POST("activities/{activityId}/rsvp")
    suspend fun rsvpToActivity(
        @Path("activityId") activityId: String,
        @Body rsvpDTO: ActivityRsvpDTO
    ): Response<ActivityDTO>

    /**
     * Cancel RSVP
     */
    @DELETE("activities/{activityId}/rsvp/{userId}")
    suspend fun cancelRsvp(
        @Path("activityId") activityId: String,
        @Path("userId") userId: String
    ): Response<Unit>

    // ==================== CALENDAR ENDPOINTS ====================

    /**
     * Get calendar activities for a user within a date range
     */
    @GET("calendar/{userId}")
    suspend fun getCalendarActivities(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<CalendarActivityDTO>>

    /**
     * Get calendar activities for a specific date
     */
    @GET("calendar/{userId}/date/{date}")
    suspend fun getCalendarActivitiesForDate(
        @Path("userId") userId: String,
        @Path("date") date: String
    ): Response<List<CalendarActivityDTO>>

    // ==================== BLOCKED USERS ENDPOINTS ====================

    /**
     * Get blocked users
     */
    @GET("blocked-users/{userId}")
    suspend fun getBlockedUsers(
        @Path("userId") userId: String
    ): Response<List<BaseUserDTO>>

    /**
     * Block a user
     */
    @POST("blocked-users")
    suspend fun blockUser(
        @Body blockUserDTO: BlockUserDTO
    ): Response<Unit>

    /**
     * Unblock a user
     */
    @DELETE("blocked-users/{userId}/{blockedUserId}")
    suspend fun unblockUser(
        @Path("userId") userId: String,
        @Path("blockedUserId") blockedUserId: String
    ): Response<Unit>

    // ==================== NOTIFICATION ENDPOINTS ====================

    /**
     * Get notifications for a user
     */
    @GET("notifications/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: String
    ): Response<List<NotificationDTO>>

    /**
     * Mark notification as read
     */
    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String
    ): Response<Unit>

    /**
     * Delete notification
     */
    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String
    ): Response<Unit>

    /**
     * Register device token for push notifications
     */
    @POST("notifications/device-token")
    suspend fun registerDeviceToken(
        @Body deviceTokenDTO: DeviceTokenDTO
    ): Response<Unit>

    // ==================== CONTACTS ENDPOINTS ====================

    /**
     * Cross-reference contacts to find friends
     */
    @POST("contacts/cross-reference")
    suspend fun crossReferenceContacts(
        @Body contactsDTO: ContactsCrossReferenceDTO
    ): Response<List<BaseUserDTO>>

    // ==================== CACHE ENDPOINTS ====================

    /**
     * Validate cache timestamps
     */
    @POST("cache/validate/{userId}")
    suspend fun validateCache(
        @Path("userId") userId: String,
        @Body cacheValidationRequest: CacheValidationRequestDTO
    ): Response<Map<String, CacheValidationResponseDTO>>

    /**
     * Clear calendar caches
     */
    @POST("cache/clear-calendar-caches")
    suspend fun clearCalendarCaches(): Response<Unit>

    // ==================== MULTIPART FORM DATA ====================

    /**
     * Upload file with multipart form data
     */
    @Multipart
    @POST("{path}")
    suspend fun uploadMultipartFormData(
        @Path("path", encoded = true) path: String,
        @Part file: MultipartBody.Part,
        @PartMap additionalData: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<ResponseBody>

    // ==================== LOCATION ENDPOINTS ====================

    /**
     * Update user location
     */
    @PATCH("users/{userId}/location")
    suspend fun updateUserLocation(
        @Path("userId") userId: String,
        @Body locationDTO: LocationUpdateDTO
    ): Response<BaseUserDTO>

    /**
     * Get nearby users
     */
    @GET("users/nearby/{userId}")
    suspend fun getNearbyUsers(
        @Path("userId") userId: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radiusKm: Double? = null
    ): Response<List<BaseUserDTO>>

    // ==================== ACTIVITY TYPES ENDPOINTS ====================

    /**
     * Get all activity types
     */
    @GET("activity-types")
    suspend fun getActivityTypes(): Response<List<ActivityTypeDTO>>

    /**
     * Get activity type by ID
     */
    @GET("activity-types/{typeId}")
    suspend fun getActivityType(
        @Path("typeId") typeId: String
    ): Response<ActivityTypeDTO>
}