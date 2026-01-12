package com.example.spawn_app_android.presentation.screens.authFlow.subComponents

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

data class GoogleSignInResult(
    val idToken: String,
    val email: String?,
    val displayName: String?,
    val profilePictureUrl: String?
)

sealed class GoogleSignInOutcome {
    data class Success(val result: GoogleSignInResult) : GoogleSignInOutcome()
    data class Error(val message: String) : GoogleSignInOutcome()
    object Cancelled : GoogleSignInOutcome()
}

suspend fun getGoogleCredential(context: Context, webClientId: String): GoogleSignInOutcome {
    // Validate webClientId
    if (webClientId.isBlank()) {
        Log.e("GoogleSignIn", "WEB_CLIENT_ID is empty! Check local.properties")
        return GoogleSignInOutcome.Error("Google Sign-In not configured. WEB_CLIENT_ID is missing.")
    }

    // Ensure we have an Activity context
    val activityContext = when (context) {
        is Activity -> context
        else -> {
            Log.e("GoogleSignIn", "Context is not an Activity: ${context.javaClass.name}")
            return GoogleSignInOutcome.Error("Invalid context for Google Sign-In")
        }
    }

    Log.d("GoogleSignIn", "Using Activity context: ${activityContext.javaClass.simpleName}")
    Log.d("GoogleSignIn", "WEB_CLIENT_ID (first 20 chars): ${webClientId.take(20)}...")

    val credentialManager = CredentialManager.create(activityContext)

    // Use GetGoogleIdOption to show the bottom sheet account picker
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(false)
        .setNonce(generateNonce())
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        Log.d("GoogleSignIn", "Initiating Google Sign-In with bottom sheet...")

        val result = credentialManager.getCredential(
            request = request,
            context = activityContext
        )

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            val idToken = googleIdTokenCredential.idToken
            val email = googleIdTokenCredential.id
            val displayName = googleIdTokenCredential.displayName
            val profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString()

            Log.d("GoogleSignIn", "Sign-in successful - Email: $email")

            GoogleSignInOutcome.Success(
                GoogleSignInResult(
                    idToken = idToken,
                    email = email,
                    displayName = displayName,
                    profilePictureUrl = profilePictureUrl
                )
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleSignIn", "Failed to parse Google ID token", e)
            GoogleSignInOutcome.Error("Failed to parse Google credentials")
        }
    } catch (e: GetCredentialCancellationException) {
        Log.d("GoogleSignIn", "Sign-in cancelled by user")
        GoogleSignInOutcome.Cancelled
    } catch (e: NoCredentialException) {
        Log.e("GoogleSignIn", "No credentials available", e)
        GoogleSignInOutcome.Error(
            "No Google account found. Please add a Google account to your device and try again."
        )
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Sign-in failed: ${e.javaClass.simpleName} - ${e.message}", e)
        GoogleSignInOutcome.Error(e.message ?: "Unknown error during Google Sign-In")
    }
}

private fun generateNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
    return hashedNonce
}

