package com.example.spawn_app_android.presentation.screens.authFlow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.BuildConfig
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.Utils.SetDarkStatusBarIcons
import com.example.spawn_app_android.presentation.screens.authFlow.subComponents.GoogleSignInOutcome
import com.example.spawn_app_android.presentation.screens.authFlow.subComponents.getGoogleCredential
import com.example.spawn_app_android.presentation.viewModels.AuthState
import com.example.spawn_app_android.presentation.viewModels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    SetDarkStatusBarIcons()
    val insets = WindowInsets.statusBars.asPaddingValues()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authState by authViewModel.authState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initialize the auth view model
    LaunchedEffect(Unit) {
        authViewModel.initialize(context)
    }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onLoginSuccess()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.white))
                .padding(top = insets.calculateTopPadding()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.create_your_account),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.sign_in_desc),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(71.dp))

            // Loading indicator
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = colorResource(R.color.activity_indigo)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Signing in...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(32.dp))
            }

            //region Sign in Partners
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginOptionButton(
                    text = stringResource(R.string.login_google),
                    icon = R.drawable.logo_google,
                    enabled = authState !is AuthState.Loading,
                    onClick = {
                        scope.launch {
                            errorMessage = null
                            when (val outcome = getGoogleCredential(
                                context = context,
                                webClientId = BuildConfig.WEB_CLIENT_ID
                            )) {
                                is GoogleSignInOutcome.Success -> {
                                    authViewModel.signInWithGoogle(
                                        context = context,
                                        idToken = outcome.result.idToken,
                                        email = outcome.result.email
                                    )
                                }
                                is GoogleSignInOutcome.Error -> {
                                    errorMessage = outcome.message
                                }
                                is GoogleSignInOutcome.Cancelled -> {
                                    // User cancelled, do nothing
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(14.dp))
                LoginOptionButton(
                    text = stringResource(R.string.login_apple),
                    icon = R.drawable.logo_apple,
                    enabled = false, // Apple sign-in not yet implemented
                    onClick = {
                        // Apple sign-in will be implemented later
                    }
                )
            }
            //endregion
        }

        // Error snackbar
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = {
                        errorMessage = null
                        authViewModel.clearError()
                    }) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun LoginOptionButton(
    text: String,
    icon: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(horizontal = 32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.white_f5f5f5),
            disabledContainerColor = colorResource(R.color.white_f5f5f5).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painterResource(id = icon),
                contentDescription = text,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp, 20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.Black else Color.Gray
            )
        }
    }
}

@Preview
@Composable
fun LoginPagePreview() {
//    LoginOptionButton("Login with Google", R.drawable.logo_google)

}
