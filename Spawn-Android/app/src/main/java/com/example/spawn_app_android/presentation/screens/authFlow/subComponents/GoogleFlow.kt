package com.example.spawn_app_android.presentation.screens.authFlow.subComponents

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialRequest.Builder
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID


// Google Token actions will be handled here (bottom Sheet popping up when Signing in with Google)
suspend fun getLoginDetails(context: Context, WEB_CLIENT_ID: String) {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .setNonce(generateNonce())
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val credentialManager = CredentialManager.create(context)

    try {
        Log.d("SignIn", "Initiating BottomSheet")

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

        val googleIdToken = googleIdTokenCredential.idToken
//        Log.d("SignIn", "Credential: $credential")
        Log.d("SignIn", "Google ID Token: $googleIdToken")
        // TODO: Extract and use token, e.g., send to backend
    } catch (e: Exception) {
        Log.e("SignIn", "Sign-in failed", e)
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

