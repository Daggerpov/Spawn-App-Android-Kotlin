package com.example.spawn_app_android.presentation.screens.authFlow.subComponents

import android.credentials.GetCredentialException
import androidx.compose.runtime.Composable
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.coroutineScope

// Google Token actions will be handled here (bottom Sheet popping up when Signing in with Google)
fun GetLoginDetails(WEB_CLIENT_ID: String) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(false)
    .build()
    return Unit
}
