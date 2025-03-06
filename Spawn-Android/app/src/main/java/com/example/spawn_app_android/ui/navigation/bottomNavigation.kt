package com.example.spawn_app_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.Color
import com.example.spawn_app_android.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.ui.theme.SpawnAppAndroidTheme


@Composable
fun BottomNavigation(
    firstIcon: Int,
    secondIcon: Int,
    selected: Int,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            NavigationBarItem(
                selected = selected == 0,
                onClick = { onFirstClick() },
                icon = {
                    Icon(
                        painter = painterResource(id = firstIcon),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                selected = selected == 1,
                onClick = { onSecondClick() },
                icon = {
                    Icon(
                        painter = painterResource(id = secondIcon),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                        tint = Color.Unspecified
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationPreview() {
    SpawnAppAndroidTheme {
        BottomNavigation(firstIcon = R.drawable.add_event_button,
            secondIcon = R.drawable.friends_button,
            selected = 0,
            onFirstClick = {},
            onSecondClick = {}
        )
    }
}