package com.example.spawn_app_android.presentation.screens.activities

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



class ActivityViewModel(
//     private val repository: ActivityRepository //for submitting data
) : ViewModel() {

    var state by mutableStateOf(CreateActivityState())
        private set

    fun onEvent(activityEvent: CreateActivityEvent) {
        when (activityEvent) {
            is CreateActivityEvent.TitleChanged -> {
                state = state.copy(title = activityEvent.title)
            }

            is CreateActivityEvent.DescriptionChanged -> {
                state = state.copy(description = activityEvent.description)
            }

            is CreateActivityEvent.LocationChanged -> {
                state = state.copy(location = activityEvent.location)
            }

            is CreateActivityEvent.TimeChanged -> {
                state = state.copy(dateTime = activityEvent.time)
            }

            is CreateActivityEvent.DurationChanged -> {
                state = state.copy(duration = activityEvent.duration)
            }

            is CreateActivityEvent.Submit -> {
                submitEvent()
            }
        }
    }

    private fun submitEvent() {
        viewModelScope.launch {
            state = state.copy(isSubmitting = true)

            try {
                // Call repository to save event
//                repository.createEvent(
//                    title = state.title,
//                    description = state.description,
//                    location = state.location,
//                    dateTime = state.dateTime
//                )

                // You could reset the form or navigate away
                state = CreateActivityState() // Reset or use UiEffect to signal success
            } catch (e: Exception) {
                state = state.copy(isSubmitting = false)
                Log.e("ActivityViewModel", "Failed to submit: ${e.localizedMessage}")
            }
        }
    }
}
