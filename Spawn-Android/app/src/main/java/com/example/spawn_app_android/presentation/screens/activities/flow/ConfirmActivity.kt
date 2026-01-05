package com.example.spawn_app_android.presentation.screens.activities.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ConfirmActivityScreen(
    onConfirm: () -> Unit,
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
        ConfirmActivityHeader(state.tag, onBack)
        ActivityTypeDisplay(state.tag)
        ActivitySummary(
            title = state.title,
            location = state.location,
            tag = state.tag
        )
        ConfirmButton(onClick = onConfirm)
        SpawnProgressIndicator(currentStep = 3, totalSteps = 3)
    }
}

@Composable
private fun ConfirmActivityHeader(type: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

        Spacer(modifier = Modifier.height(6.5.dp))

        Text(
            "Review your \"$type\" activity",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.text_contrast),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ActivityTypeDisplay(tag: String) {
    Column(
        modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.secondary_bg)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = getActivityIcon(tag)),
            contentDescription = tag,
            tint = Color.Unspecified,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = tag,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActivitySummary(
    title: String,
    location: String,
    tag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 37.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryItem(label = "Title", value = title.ifEmpty { "No title" })
        SummaryItem(label = "Location", value = location.ifEmpty { "No location selected" })
        SummaryItem(label = "Activity Type", value = tag.ifEmpty { "Not specified" })
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.text_contrast)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ConfirmButton(onClick: () -> Unit) {
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
            text = "Confirm Activity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun getActivityIcon(tag: String): Int {
    return when (tag.lowercase()) {
        "eat" -> R.drawable.ic_eat
        "gym" -> R.drawable.ic_gym
        "study" -> R.drawable.ic_pencil
        "chill" -> R.drawable.ic_chill
        "hike" -> R.drawable.ic_hike
        else -> R.drawable.ic_eat
    }
}
