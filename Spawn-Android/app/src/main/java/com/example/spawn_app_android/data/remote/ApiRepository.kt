package com.example.spawn_app_android.data.remote

import android.util.Log
import com.example.spawn_app_android.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

/**
 * ApiRepository.kt
 *
 * Created by Ethan Dsouza on 2025-12-06
 *
 * Repository class that wraps ApiService calls with error handling.
 * Mirrors the iOS Swift APIService fetch/send/update/delete methods.
 */

class ApiRepository(
    private val apiService: ApiService = ApiClient.getApiService()
) {

    companion object {
        private const val TAG = "ApiRepository"
    }

    // ==================== GENERIC API CALL HANDLERS ====================

    /**
     * Generic handler for API responses with error handling.
     * Mirrors iOS fetchData/sendData pattern.
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "API call failed: ${e.message}", e)
            ApiResult.Error(
                ApiError.NetworkError(e.message ?: "Unknown network error")
            )
        }
    }

    /**
     * Handle API response and convert to ApiResult
     */
    private fun <T> handleResponse(response: Response<T>): ApiResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                // Handle 204 No Content or empty successful responses
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }
        } else {
            val errorMessage = parseErrorMessage(response)
            Log.e(TAG, "API Error (${response.code()}): $errorMessage")
            
            when (response.code()) {
                401 -> ApiResult.Error(ApiError.Unauthorized(errorMessage))
                403 -> ApiResult.Error(ApiError.Forbidden(errorMessage))
                404 -> ApiResult.Error(ApiError.NotFound(errorMessage))
                422 -> ApiResult.Error(ApiError.ValidationError(errorMessage))
                500, 502, 503 -> ApiResult.Error(ApiError.ServerError(errorMessage))
                else -> ApiResult.Error(ApiError.HttpError(response.code(), errorMessage))
            }
        }
    }

    /**
     * Parse error message from response body
     */
    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            response.errorBody()?.string() ?: "Unknown error"
        } catch (e: Exception) {
            "Error parsing error response: ${e.message}"
        }
    }

    // ==================== AUTH METHODS ====================

    suspend fun signIn(email: String, password: String? = null): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.signIn(SignInRequestDTO(email, password))
        }
    }

    suspend fun loginWithOAuth(email: String, idToken: String): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.login(OAuthLoginRequestDTO(email, idToken))
        }
    }

    suspend fun quickSignIn(email: String, deviceId: String? = null): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.quickSignIn(QuickSignInRequestDTO(email, deviceId))
        }
    }

    suspend fun registerWithOAuth(
        email: String,
        idToken: String,
        name: String? = null,
        profilePictureUrl: String? = null
    ): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.registerWithOAuth(
                OAuthRegisterRequestDTO(email, idToken, name = name, profilePictureUrl = profilePictureUrl)
            )
        }
    }

    suspend fun createUser(userCreateDTO: UserCreateDTO, profilePicUrl: String? = null): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.createUser(userCreateDTO, profilePicUrl)
        }
    }

    suspend fun sendVerificationCode(email: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.sendVerificationCode(VerificationSendRequestDTO(email))
        }
    }

    suspend fun checkVerificationCode(email: String, code: String): ApiResult<VerificationCheckResponseDTO> {
        return safeApiCall {
            apiService.checkVerificationCode(VerificationCheckRequestDTO(email, code))
        }
    }

    suspend fun refreshToken(): ApiResult<Unit> {
        val refreshToken = ApiClient.getTokenManager()?.getRefreshToken()
            ?: return ApiResult.Error(ApiError.Unauthorized("No refresh token available"))
        
        return safeApiCall {
            apiService.refreshToken("Bearer $refreshToken")
        }
    }

    // ==================== USER METHODS ====================

    suspend fun getUser(userId: String): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.getUser(userId)
        }
    }

    suspend fun getAllUsers(): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.getAllUsers()
        }
    }

    suspend fun updateUser(userId: String, userUpdateDTO: UserUpdateDTO): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.updateUser(userId, userUpdateDTO)
        }
    }

    suspend fun deleteUser(userId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.deleteUser(userId)
        }
    }

    suspend fun updateProfilePicture(userId: String, imageBytes: ByteArray): ApiResult<BaseUserDTO> {
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return safeApiCall {
            apiService.updateProfilePicture(userId, requestBody)
        }
    }

    suspend fun updateOptionalDetails(userId: String, optionalDetails: OptionalDetailsDTO): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.updateOptionalDetails(userId, optionalDetails)
        }
    }

    suspend fun searchUsers(query: String, excludeUserId: String? = null): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.searchUsers(query, excludeUserId)
        }
    }

    suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double): ApiResult<BaseUserDTO> {
        return safeApiCall {
            apiService.updateUserLocation(userId, LocationUpdateDTO(latitude, longitude))
        }
    }

    suspend fun getNearbyUsers(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double? = null
    ): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.getNearbyUsers(userId, latitude, longitude, radiusKm)
        }
    }

    // ==================== FRIEND METHODS ====================

    suspend fun getFriends(userId: String): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.getFriends(userId)
        }
    }

    suspend fun sendFriendRequest(senderId: String, receiverId: String): ApiResult<FriendRequestResponseDTO> {
        return safeApiCall {
            apiService.sendFriendRequest(FriendRequestDTO(senderId, receiverId))
        }
    }

    suspend fun acceptFriendRequest(requestId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.acceptFriendRequest(requestId)
        }
    }

    suspend fun declineFriendRequest(requestId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.declineFriendRequest(requestId)
        }
    }

    suspend fun getPendingFriendRequests(userId: String): ApiResult<List<FriendRequestResponseDTO>> {
        return safeApiCall {
            apiService.getPendingFriendRequests(userId)
        }
    }

    suspend fun getSentFriendRequests(userId: String): ApiResult<List<FriendRequestResponseDTO>> {
        return safeApiCall {
            apiService.getSentFriendRequests(userId)
        }
    }

    suspend fun removeFriend(userId: String, friendId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.removeFriend(userId, friendId)
        }
    }

    // ==================== FRIEND TAG METHODS ====================

    suspend fun getFriendTags(userId: String): ApiResult<List<FriendTagDTO>> {
        return safeApiCall {
            apiService.getFriendTags(userId)
        }
    }

    suspend fun createFriendTag(friendTagCreateDTO: FriendTagCreateDTO): ApiResult<FriendTagDTO> {
        return safeApiCall {
            apiService.createFriendTag(friendTagCreateDTO)
        }
    }

    suspend fun updateFriendTag(tagId: String, friendTagUpdateDTO: FriendTagUpdateDTO): ApiResult<FriendTagDTO> {
        return safeApiCall {
            apiService.updateFriendTag(tagId, friendTagUpdateDTO)
        }
    }

    suspend fun deleteFriendTag(tagId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.deleteFriendTag(tagId)
        }
    }

    suspend fun addFriendToTag(tagId: String, friendId: String): ApiResult<FriendTagDTO> {
        return safeApiCall {
            apiService.addFriendToTag(tagId, friendId)
        }
    }

    suspend fun removeFriendFromTag(tagId: String, friendId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.removeFriendFromTag(tagId, friendId)
        }
    }

    // ==================== ACTIVITY METHODS ====================

    suspend fun getUserActivities(userId: String): ApiResult<List<ActivityDTO>> {
        return safeApiCall {
            apiService.getUserActivities(userId)
        }
    }

    suspend fun getActivity(activityId: String): ApiResult<ActivityDTO> {
        return safeApiCall {
            apiService.getActivity(activityId)
        }
    }

    suspend fun createActivity(activityCreateDTO: ActivityCreateDTO): ApiResult<ActivityDTO> {
        return safeApiCall {
            apiService.createActivity(activityCreateDTO)
        }
    }

    suspend fun updateActivity(activityId: String, activityUpdateDTO: ActivityUpdateDTO): ApiResult<ActivityDTO> {
        return safeApiCall {
            apiService.updateActivity(activityId, activityUpdateDTO)
        }
    }

    suspend fun deleteActivity(activityId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.deleteActivity(activityId)
        }
    }

    suspend fun getFeedActivities(userId: String): ApiResult<List<ActivityDTO>> {
        return safeApiCall {
            apiService.getFeedActivities(userId)
        }
    }

    suspend fun rsvpToActivity(activityId: String, userId: String, status: String = "attending"): ApiResult<ActivityDTO> {
        return safeApiCall {
            apiService.rsvpToActivity(activityId, ActivityRsvpDTO(userId, status))
        }
    }

    suspend fun cancelRsvp(activityId: String, userId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.cancelRsvp(activityId, userId)
        }
    }

    // ==================== CALENDAR METHODS ====================

    suspend fun getCalendarActivities(
        userId: String,
        startDate: String,
        endDate: String
    ): ApiResult<List<CalendarActivityDTO>> {
        return safeApiCall {
            apiService.getCalendarActivities(userId, startDate, endDate)
        }
    }

    suspend fun getCalendarActivitiesForDate(userId: String, date: String): ApiResult<List<CalendarActivityDTO>> {
        return safeApiCall {
            apiService.getCalendarActivitiesForDate(userId, date)
        }
    }

    // ==================== BLOCKED USERS METHODS ====================

    suspend fun getBlockedUsers(userId: String): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.getBlockedUsers(userId)
        }
    }

    suspend fun blockUser(blockerId: String, blockedId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.blockUser(BlockUserDTO(blockerId, blockedId))
        }
    }

    suspend fun unblockUser(userId: String, blockedUserId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.unblockUser(userId, blockedUserId)
        }
    }

    // ==================== NOTIFICATION METHODS ====================

    suspend fun getNotifications(userId: String): ApiResult<List<NotificationDTO>> {
        return safeApiCall {
            apiService.getNotifications(userId)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.markNotificationAsRead(notificationId)
        }
    }

    suspend fun deleteNotification(notificationId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.deleteNotification(notificationId)
        }
    }

    suspend fun registerDeviceToken(userId: String, token: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.registerDeviceToken(DeviceTokenDTO(userId, token))
        }
    }

    // ==================== CONTACTS METHODS ====================

    suspend fun crossReferenceContacts(
        userId: String,
        phoneNumbers: List<String>? = null,
        emails: List<String>? = null
    ): ApiResult<List<BaseUserDTO>> {
        return safeApiCall {
            apiService.crossReferenceContacts(ContactsCrossReferenceDTO(userId, phoneNumbers, emails))
        }
    }

    // ==================== CACHE METHODS ====================

    suspend fun validateCache(
        userId: String,
        timestamps: Map<String, String>
    ): ApiResult<Map<String, CacheValidationResponseDTO>> {
        return safeApiCall {
            apiService.validateCache(userId, CacheValidationRequestDTO(timestamps))
        }
    }

    suspend fun clearCalendarCaches(): ApiResult<Unit> {
        return safeApiCall {
            apiService.clearCalendarCaches()
        }
    }

    // ==================== ACTIVITY TYPES METHODS ====================

    suspend fun getActivityTypes(): ApiResult<List<ActivityTypeDTO>> {
        return safeApiCall {
            apiService.getActivityTypes()
        }
    }

    suspend fun getActivityType(typeId: String): ApiResult<ActivityTypeDTO> {
        return safeApiCall {
            apiService.getActivityType(typeId)
        }
    }

    // ==================== FILE UPLOAD METHODS ====================

    suspend fun uploadFile(
        path: String,
        file: File,
        additionalData: Map<String, String> = emptyMap()
    ): ApiResult<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val additionalParts = additionalData.mapValues { (_, value) ->
                value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            val response = apiService.uploadMultipartFormData(path, filePart, additionalParts)
            
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.bytes() ?: ByteArray(0))
            } else {
                ApiResult.Error(ApiError.HttpError(response.code(), parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "File upload failed: ${e.message}", e)
            ApiResult.Error(ApiError.NetworkError(e.message ?: "Unknown error"))
        }
    }
}

// ==================== API RESULT SEALED CLASS ====================

/**
 * Sealed class representing API call results.
 * Provides type-safe success/error handling.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: ApiError) : ApiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (ApiError) -> Unit): ApiResult<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

// ==================== API ERROR SEALED CLASS ====================

/**
 * Sealed class representing different types of API errors.
 * Mirrors iOS APIError enum.
 */
sealed class ApiError(open val message: String) {
    data class NetworkError(override val message: String) : ApiError(message)
    data class HttpError(val statusCode: Int, override val message: String) : ApiError(message)
    data class Unauthorized(override val message: String) : ApiError(message)
    data class Forbidden(override val message: String) : ApiError(message)
    data class NotFound(override val message: String) : ApiError(message)
    data class ValidationError(override val message: String) : ApiError(message)
    data class ServerError(override val message: String) : ApiError(message)
    data class ParseError(override val message: String) : ApiError(message)
    object Cancelled : ApiError("Request was cancelled")

    fun isUnauthorized(): Boolean = this is Unauthorized
    fun isNotFound(): Boolean = this is NotFound
    fun isNetworkError(): Boolean = this is NetworkError
}
