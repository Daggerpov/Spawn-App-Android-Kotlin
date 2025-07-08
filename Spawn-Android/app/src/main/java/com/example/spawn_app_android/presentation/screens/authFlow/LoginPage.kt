package com.example.spawn_app_android.presentation.screens.authFlow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit
) {
    val isLoggedIn = remember { /* check from ViewModel */ false }

    LaunchedEffect(true) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.activity_indigo)),
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

        Spacer(Modifier.height(32.dp))
        LoginOptionButton(stringResource(R.string.login_google), R.drawable.logo_google)
//        LoginOptionButton(stringResource(R.string.login_apple))

    }
}

@Composable
private fun LoginOptionButton(text: String, icon: Int) {
    Button(
        onClick = {},
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.activity_indigo))
            .padding(32.dp, 20.dp), // margin
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp), //padding
//                .background(colorResource(R.color.activity_indigo)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(painterResource(id = icon), contentDescription = text, tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color.Black)
        }
    }
}

@Preview
@Composable
fun LoginPagePreview() {
    LoginOptionButton("Login with Google", R.drawable.logo_google)

}
