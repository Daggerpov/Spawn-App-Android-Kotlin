package com.example.spawn_app_android.presentation.screens.activities.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.Utils.getNotifBarPadding
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel
import com.example.spawn_app_android.presentation.screens.activities.CreateActivityEvent
import com.example.spawn_app_android.presentation.screens.activities.CreateActivityState
import java.util.Calendar

@Composable
fun SetActivityTimeScreen(
    onNext: () -> Unit,
    activityViewModel: ActivityViewModel
) {
    var state = activityViewModel.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = getNotifBarPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        CustomTimePicker()
        CustomTextField(activityViewModel, state)
        LabelSelector()
        NextStepButton(onClick = onNext)
        SpawnProgressIndicator(currentStep = 1, totalSteps = 3)
    }
}

@Composable
fun NextStepButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5A78FF), // blue-violet
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 37.dp)
    ) {
        Text(
            text = "Next Step (Location)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SpawnProgressIndicator(currentStep: Int, totalSteps: Int = 3) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        color = if (index < currentStep) colorResource(R.color.icon_positive) else Color.LightGray
                    )
            )
        }
    }
}


@Composable
fun SelectableLabel(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF4C6FFF) else Color.Gray
    val textColor = if (selected) Color(0xFF4C6FFF) else Color.Gray
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun LabelSelector() {
    val labels = listOf("Indefinite", "2 hours", "1 hour", "30 min", "Custom")
    var selectedLabel by remember { mutableStateOf("Indefinite") }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(labels) { label ->
            SelectableLabel(
                text = label,
                selected = label == selectedLabel,
                onClick = { selectedLabel = label }
            )
        }
    }
}

@Composable
private fun CustomTextField(activityViewModel: ActivityViewModel, state: CreateActivityState) {
    Column(
        modifier = Modifier
            .padding(37.dp, 12.dp)
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(text = stringResource(R.string.title), style = MaterialTheme.typography.bodyMedium)
        TextField(
            modifier = Modifier
                .padding(0.dp, 12.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.white))
                .border(2.dp, colorResource(R.color.text_contrast), RoundedCornerShape(8.dp))
                .padding(horizontal = 0.dp),
            value = state.title,
            onValueChange = { activityViewModel.onEvent(CreateActivityEvent.TitleChanged(it)) },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                // For Background
                focusedContainerColor = colorResource(R.color.white),
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.Gray,
                // For Text
                focusedTextColor = colorResource(R.color.black),
                unfocusedTextColor = colorResource(R.color.black),
                disabledTextColor = Color.Black
            ),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.enter_activity_title),
                    color = colorResource(R.color.white),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

@Composable
private fun CustomTimePicker() {
    DialExample(onConfirm = {}, onDismiss = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialExample(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,

    )

    Column {
        TimeInput(
            state = timePickerState,
        )
//        Button(onClick = onDismiss) {
//            Text("Dismiss picker")
//        }
//        Button(onClick = onConfirm) {
//            Text("Confirm selection")
//        }
    }
}


//@Preview
//@Composable
//fun SetActivityTimePreview() {
//    SpawnEditText("Title", "Enter Activity Title")
//}