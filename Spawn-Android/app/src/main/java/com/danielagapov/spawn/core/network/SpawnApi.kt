package com.danielagapov.spawn.core.network

import com.danielagapov.spawn.core.model.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/** Spawn REST API (same endpoints the iOS app calls; see iOS ReadOperationConfig/WriteOperationConfig). */
interface SpawnApi {

    // region Auth
    @GET("auth/sign-in")
    suspend fun signIn(
        @Query("idToken") idToken: String,
        @Query("email") email: String,
        @Query("provider") provider: String,
    ): Response<AuthResponseDTO>

    @POST("auth/login")
    suspend fun login(@Body body: LoginDTO): Response<AuthResponseDTO>

    @GET("auth/quick-sign-in")
    suspend fun quickSignIn(): Response<AuthResponseDTO>

    @POST("auth/register/oauth")
    suspend fun registerOAuth(@Body body: OAuthRegistrationDTO): Response<AuthResponseDTO>

    @POST("auth/register/verification/send")
    suspend fun sendVerification(@Body body: EmailVerificationSendDTO): Response<EmailVerificationResponseDTO>

    @POST("auth/register/verification/check")
    suspend fun checkVerification(@Body body: EmailVerificationVerifyDTO): Response<AuthResponseDTO>

    @POST("auth/user/details")
    suspend fun updateUserDetails(@Body body: UpdateUserDetailsDTO): Response<BaseUserDTO>

    @POST("users/{id}/optional-details")
    suspend fun setOptionalDetails(@Path("id") userId: String, @Body body: OptionalDetailsDTO): Response<BaseUserDTO>

    @POST("auth/complete-contact-import/{id}")
    suspend fun completeContactImport(@Path("id") userId: String): Response<BaseUserDTO>

    @POST("auth/accept-tos/{id}")
    suspend fun acceptTos(@Path("id") userId: String): Response<BaseUserDTO>
    // endregion

    // region Users / profile
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String, @Query("requestingUserId") requestingUserId: String? = null): Response<BaseUserDTO>

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body body: UserUpdateDTO): Response<BaseUserDTO>

    @PATCH("users/{id}/profile-picture")
    suspend fun updateProfilePicture(@Path("id") id: String, @Body bytes: RequestBody): Response<BaseUserDTO>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    @GET("users/{id}/stats")
    suspend fun stats(@Path("id") id: String): Response<UserStatsDTO>

    @GET("users/{id}/interests")
    suspend fun interests(@Path("id") id: String): Response<List<String>>

    @POST("users/{id}/interests")
    suspend fun addInterest(@Path("id") id: String, @Body interest: RequestBody): Response<ResponseBody>

    @PUT("users/{id}/interests")
    suspend fun setInterests(@Path("id") id: String, @Body interests: List<String>): Response<ResponseBody>

    @DELETE("users/{id}/interests/{interest}")
    suspend fun removeInterest(@Path("id") id: String, @Path("interest") interest: String): Response<ResponseBody>

    @GET("users/{id}/social-media")
    suspend fun socialMedia(@Path("id") id: String): Response<UserSocialMediaDTO>

    @PUT("users/{id}/social-media")
    suspend fun updateSocialMedia(@Path("id") id: String, @Body body: UpdateUserSocialMediaDTO): Response<UserSocialMediaDTO>

    @GET("users/{id}/calendar")
    suspend fun calendar(
        @Path("id") id: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("timezone") timezone: String,
        @Query("requestingUserId") requestingUserId: String? = null,
    ): Response<List<CalendarActivityDTO>>

    @GET("users/{id}/recent-users")
    suspend fun recentlySpawnedWith(@Path("id") id: String): Response<List<RecentlySpawnedUserDTO>>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("searchQuery") query: String,
        @Query("requestingUserId") requestingUserId: String,
    ): Response<List<BaseUserDTO>>

    @GET("users/filtered/{id}")
    suspend fun filteredUsers(@Path("id") id: String, @Query("searchQuery") query: String): Response<SearchedUserResult>
    // endregion

    // region Friends
    @GET("users/friends/{id}")
    suspend fun friends(@Path("id") id: String): Response<List<FullFriendUserDTO>>

    @GET("users/friends-minimal/{id}")
    suspend fun minimalFriends(@Path("id") id: String): Response<List<MinimalFriendDTO>>

    @GET("users/recommended-friends/{id}")
    suspend fun recommendedFriends(@Path("id") id: String): Response<List<RecommendedFriendUserDTO>>

    @DELETE("users/friends/{userId}/{friendId}")
    suspend fun removeFriend(@Path("userId") userId: String, @Path("friendId") friendId: String): Response<ResponseBody>

    @GET("friend-requests/incoming/{id}")
    suspend fun incomingFriendRequests(@Path("id") id: String): Response<List<FetchFriendRequestDTO>>

    @GET("friend-requests/sent/{id}")
    suspend fun sentFriendRequests(@Path("id") id: String): Response<List<FetchSentFriendRequestDTO>>

    @POST("friend-requests")
    suspend fun sendFriendRequest(@Body body: CreateFriendRequestDTO): Response<CreateFriendRequestDTO>

    /** action: accept | reject | cancel */
    @PUT("friend-requests/{id}")
    suspend fun friendRequestAction(@Path("id") id: String, @Query("friendRequestAction") action: String): Response<ResponseBody>
    // endregion

    // region Activities
    @GET("activities/feed-activities/{id}")
    suspend fun feedActivities(@Path("id") userId: String): Response<List<FullFeedActivityDTO>>

    @GET("activities/{id}")
    suspend fun activity(
        @Path("id") id: String,
        @Query("requestingUserId") requestingUserId: String,
        @Query("autoJoin") autoJoin: Boolean? = null,
    ): Response<FullFeedActivityDTO>

    @GET("activities/profile/{id}")
    suspend fun profileActivities(
        @Path("id") profileUserId: String,
        @Query("requestingUserId") requestingUserId: String,
    ): Response<List<FullFeedActivityDTO>>

    @POST("activities")
    suspend fun createActivity(@Body body: ActivityDTO): Response<ActivityCreationResponseDTO>

    @PATCH("activities/{id}/partial")
    suspend fun updateActivity(@Path("id") id: String, @Body body: ActivityPartialUpdateDTO): Response<FullFeedActivityDTO>

    @DELETE("activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String): Response<ResponseBody>

    @PUT("activities/{activityId}/toggle-status/{userId}")
    suspend fun toggleParticipation(@Path("activityId") activityId: String, @Path("userId") userId: String): Response<FullFeedActivityDTO>

    @GET("activities/{id}/chats")
    suspend fun chats(@Path("id") activityId: String): Response<List<FullActivityChatMessageDTO>>

    @GET("users/{id}/activity-types")
    suspend fun activityTypes(@Path("id") userId: String): Response<List<ActivityTypeDTO>>

    @PUT("users/{id}/activity-types")
    suspend fun updateActivityTypes(@Path("id") userId: String, @Body body: BatchActivityTypeUpdateDTO): Response<List<ActivityTypeDTO>>
    // endregion

    // region Chat
    @POST("chat-messages")
    suspend fun sendChat(@Body body: CreateChatMessageDTO): Response<FullActivityChatMessageDTO>

    @DELETE("chat-messages/{id}")
    suspend fun deleteChat(@Path("id") id: String): Response<ResponseBody>

    @POST("chat-messages/{id}/likes/{userId}")
    suspend fun likeChat(@Path("id") id: String, @Path("userId") userId: String): Response<ChatMessageLikesDTO>

    @DELETE("chat-messages/{id}/likes/{userId}")
    suspend fun unlikeChat(@Path("id") id: String, @Path("userId") userId: String): Response<ResponseBody>
    // endregion

    // region Safety
    @POST("blocked-users")
    suspend fun blockUser(@Body body: BlockedUserCreationDTO): Response<ResponseBody>

    @DELETE("blocked-users/{blockerId}/{blockedId}")
    suspend fun unblockUser(@Path("blockerId") blockerId: String, @Path("blockedId") blockedId: String): Response<ResponseBody>

    @GET("blocked-users/{blockerId}")
    suspend fun blockedUsers(@Path("blockerId") blockerId: String, @Query("returnOnlyIds") onlyIds: Boolean = false): Response<List<BlockedUserDTO>>

    @POST("reports")
    suspend fun report(@Body body: CreateReportedContentDTO): Response<ResponseBody>

    @POST("feedback")
    suspend fun feedback(@Body body: FeedbackSubmissionDTO): Response<ResponseBody>
    // endregion

    // region Notifications
    @POST("notifications/device-tokens")
    suspend fun registerDeviceToken(@Body body: DeviceTokenDTO): Response<ResponseBody>
    // endregion
}
