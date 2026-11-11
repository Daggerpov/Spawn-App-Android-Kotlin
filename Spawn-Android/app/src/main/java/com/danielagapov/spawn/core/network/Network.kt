package com.danielagapov.spawn.core.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.danielagapov.spawn.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Access + refresh JWTs, kept in EncryptedSharedPreferences (Android's equivalent of the iOS Keychain). */
class TokenStore(context: Context) {
    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "spawn_secure_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        // Keystore can be wiped (e.g. after a backup restore). Start fresh rather than crash.
        Log.w("TokenStore", "Encrypted prefs unavailable, resetting", e)
        context.deleteSharedPreferences("spawn_secure_prefs")
        EncryptedSharedPreferences.create(
            context,
            "spawn_secure_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var accessToken: String?
        get() = prefs.getString("accessToken", null)
        set(value) = prefs.edit().putString("accessToken", value).apply()

    var refreshToken: String?
        get() = prefs.getString("refreshToken", null)
        set(value) = prefs.edit().putString("refreshToken", value).apply()

    val hasTokens: Boolean get() = accessToken != null || refreshToken != null

    fun clear() = prefs.edit().remove("accessToken").remove("refreshToken").apply()
}

private val UNAUTHENTICATED_PATHS = listOf(
    "auth/register/verification/send",
    "auth/register/oauth",
    "auth/sign-in",
    "auth/login",
    "auth/refresh-token",
    "contacts/cross-reference",
)

/** Adds the bearer token to requests and saves any tokens the backend hands back in response headers. */
class AuthInterceptor(private val tokens: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath
        val builder = req.newBuilder()
        if (UNAUTHENTICATED_PATHS.none { path.endsWith(it) } && req.header("Authorization") == null) {
            tokens.accessToken?.let { builder.header("Authorization", "Bearer $it") }
        }
        val response = chain.proceed(builder.build())
        saveTokens(response, tokens)
        return response
    }
}

internal fun saveTokens(response: Response, tokens: TokenStore) {
    response.header("Authorization")?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }?.let {
        tokens.accessToken = it
    }
    response.header("X-Refresh-Token")?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }?.let {
        tokens.refreshToken = it
    }
}

/**
 * On a 401, swaps the refresh token for a new access token (POST auth/refresh-token with the refresh
 * token as bearer, same as iOS) and retries once. If that fails the session is over.
 */
class TokenAuthenticator(
    private val tokens: TokenStore,
    private val baseUrl: String,
    private val onSessionExpired: () -> Unit,
) : Authenticator {
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (UNAUTHENTICATED_PATHS.any { path.endsWith(it) }) return null
        if (responseCount(response) >= 2) return null
        val refresh = tokens.refreshToken ?: return null.also { onSessionExpired() }

        synchronized(this) {
            // Another request may already have refreshed while we waited.
            val current = tokens.accessToken
            val sent = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (current != null && current != sent) {
                return response.request.newBuilder().header("Authorization", "Bearer $current").build()
            }
            val refreshReq = Request.Builder()
                .url(baseUrl + "auth/refresh-token")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .header("Authorization", "Bearer $refresh")
                .build()
            return try {
                refreshClient.newCall(refreshReq).execute().use { r ->
                    val newToken = r.header("Authorization")?.removePrefix("Bearer ")
                    if (r.isSuccessful && !newToken.isNullOrBlank()) {
                        tokens.accessToken = newToken
                        r.header("X-Refresh-Token")?.removePrefix("Bearer ")?.let { tokens.refreshToken = it }
                        response.request.newBuilder().header("Authorization", "Bearer $newToken").build()
                    } else {
                        if (r.code in listOf(400, 401, 403, 404)) {
                            tokens.clear()
                            onSessionExpired()
                        }
                        null
                    }
                }
            } catch (e: Exception) {
                Log.w("TokenAuthenticator", "Refresh failed", e)
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response
        var count = 1
        while (r?.priorResponse != null) {
            count++; r = r.priorResponse
        }
        return count
    }
}

val SpawnJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = true
}

object NetworkModule {
    fun okHttp(tokens: TokenStore, onSessionExpired: () -> Unit): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokens))
            .authenticator(TokenAuthenticator(tokens, BuildConfig.API_BASE_URL, onSessionExpired))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()

    fun api(client: OkHttpClient): SpawnApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(SpawnJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SpawnApi::class.java)
}

/** Thrown for non-2xx responses so view models can show something human. */
class ApiException(val code: Int, message: String) : Exception(message)

/** Unwraps a Retrofit response or throws [ApiException] with a friendly message. */
fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        @Suppress("UNCHECKED_CAST")
        return body() ?: (Unit as T)
    }
    val raw = try { errorBody()?.string() } catch (_: Exception) { null }
    val serverMessage = raw?.let { Regex("\"(message|error)\"\\s*:\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(2) }
    val msg = when (code()) {
        401 -> "Your session expired. Please sign in again."
        403 -> serverMessage ?: "You don't have permission to do that."
        404 -> serverMessage ?: "We couldn't find that."
        409 -> serverMessage ?: "That's already taken."
        429 -> "Too many attempts. Try again in a bit."
        in 500..599 -> "Something went wrong on our end. Try again."
        else -> serverMessage ?: "Request failed (${code()})."
    }
    throw ApiException(code(), msg)
}

fun Throwable.friendlyMessage(): String = when (this) {
    is ApiException -> message ?: "Something went wrong."
    is java.net.UnknownHostException, is java.net.ConnectException -> "You're offline. Check your connection."
    is java.net.SocketTimeoutException -> "That took too long. Try again."
    else -> message ?: "Something went wrong."
}
