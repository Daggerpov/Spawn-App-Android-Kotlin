package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.presentation.screens.home.HomeScreen
import com.example.spawn_app_android.presentation.viewModels.HomeViewModel

val vm = HomeViewModel()

@Composable fun HomeScreen() = HomeScreen(vm)
@Composable fun MapScreen() = ScreenTemplate("Map")
@Composable fun ActivitesScreen() = ScreenTemplate("Activities")
@Composable fun FriendsScreen() = ScreenTemplate("Friends")
@Composable fun ProfileScreen() = ScreenTemplate("Profile")

@Composable
fun ScreenTemplate(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title Screen",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp
        )
    }
}
