package com.example.spawn_app_android.presentation.screens.activities.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.Utils.getNotifBarPadding
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel

@Composable
fun ActivitySuccessScreen(
    onDone: () -> Unit,
    activityViewModel: ActivityViewModel,
    onBack: () -> Unit
) {
    val state = activityViewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = getNotifBarPadding() + 30.dp,
                bottom = 34.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SuccessHeader(onBack)

        Icon(
            painter = painterResource(id = R.drawable.ic_check_circle_broken),
            contentDescription = "Success",
            tint = Color.Unspecified
        )

        SuccessMessage(title = state.title, tag = state.tag)
        DoneButton(onClick = onDone)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SuccessHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
    ) {
        BackButton(onClick = onBack, icon = R.drawable.ic_back_black)

        Text(
            stringResource(R.string.confirm_activity),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SuccessMessage(title:String, tag: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 37.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Your \"$tag\" activity is live!",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "You’ve spawned in and “$title” is now live for your friends.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.text_contrast),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DoneButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5A78FF),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 37.dp)
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
