package com.example.spawn_app_android.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spawn_app_android.domain.model.ActivityModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {
    private val _allActivities = MutableStateFlow<List<ActivityModel>>(emptyList())
    private val _selectedFilter = MutableStateFlow<String?>(null)

    val filteredActivities: StateFlow<List<ActivityModel>> = combine(
        _allActivities, _selectedFilter
    ) { activities, filter ->
        filter?.let { tag ->
            activities.filter { it.tag.equals(tag, ignoreCase = true) }
        } ?: activities
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Replace this with API call
        _allActivities.value = sampleActivities()
    }

    fun setFilter(tag: String?) {
        _selectedFilter.value = tag
    }

    fun getActivities(): List<ActivityModel> {
        return _allActivities.value
    }

    private fun sampleActivities(): List<ActivityModel> {
        return listOf(
            ActivityModel(
                id = "1",
                title = "Late Night Ramen Run",
                host = "Alex Chen",
                time = "10:30 PM",
                location = "Downtown Ramen Bar",
                distance = "2.1 km",
                tag = "EAT",
                status = "HAPPENING NOW"
            ),
            ActivityModel(
                id = "2",
                title = "Group Workout Session",
                host = "Jamie Lee",
                time = "7:00 AM",
                location = "UBC Gymnasium",
                distance = "0.8 km",
                tag = "GYM",
                status = "IN 3 HOURS"
            ),
            ActivityModel(
                id = "3",
                title = "CS Final Review",
                host = "Taylor Wong",
                time = "6:00 PM",
                location = "Irving K. Barber Library",
                distance = "0.3 km",
                tag = "STUDY",
                status = "HAPPENING NOW"
            ),
            ActivityModel(
                id = "4",
                title = "Korean Fried Chicken Night",
                host = "Rina Patel",
                time = "9:00 PM",
                location = "Seoul House",
                distance = "1.5 km",
                tag = "EAT",
                status = "IN 3 HOURS"
            ),
            ActivityModel(
                id = "5",
                title = "Midday Lift",
                host = "Chris Park",
                time = "12:00 PM",
                location = "Gold’s Gym UBC",
                distance = "0.6 km",
                tag = "GYM",
                status = "HAPPENING NOW"
            )
        )
    }
}