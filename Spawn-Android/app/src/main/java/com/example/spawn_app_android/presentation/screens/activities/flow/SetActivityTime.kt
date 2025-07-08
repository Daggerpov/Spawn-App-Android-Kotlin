package com.example.spawn_app_android.presentation.screens.activities.flow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import java.util.Calendar

@Composable
fun SetActivityTimeScreen(vmActivity: ViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomTimePicker()

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
        TimePicker(
            state = timePickerState,
        )
        Button(onClick = onDismiss) {
            Text("Dismiss picker")
        }
        Button(onClick = onConfirm) {
            Text("Confirm selection")
        }
    }
}