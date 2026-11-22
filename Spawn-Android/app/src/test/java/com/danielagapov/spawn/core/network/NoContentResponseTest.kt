package com.danielagapov.spawn.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The server answers several actions (accepting a friend request, deleting an activity, ...) with
 * 204 No Content. Those calls must come back as success, not a ClassCastException (issue #86).
 */
class NoContentResponseTest {
    private fun apiAnswering(code: Int): SpawnApi {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder()
                .request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(code).message("No Content")
                .body("".toResponseBody(null))
                .build()
        }.build()
        return NetworkModule.api(client)
    }

    @Test
    fun acceptingAFriendRequestThatReturns204Succeeds() = runBlocking {
        // Same shape as FriendsViewModel.accept
        val result = runCatching { apiAnswering(204).friendRequestAction("request-id", "accept").bodyOrThrow() }
        assertTrue(result.exceptionOrNull()?.toString(), result.isSuccess)
    }

    @Test
    fun otherNoContentActionsSucceed() = runBlocking {
        val api = apiAnswering(204)
        val calls = listOf<suspend () -> Unit>(
            { api.friendRequestAction("request-id", "reject").bodyOrThrow() },
            { api.removeFriend("me", "friend").bodyOrThrow() },
            { api.deleteActivity("activity-id").bodyOrThrow() },
            { api.unblockUser("me", "them").bodyOrThrow() },
        )
        calls.forEach { call -> val r = runCatching { call() }; assertTrue(r.exceptionOrNull()?.toString(), r.isSuccess) }
    }
}
