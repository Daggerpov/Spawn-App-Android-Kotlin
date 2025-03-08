package com.example.spawn_app_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
    onSecondClick: () -> Unit,
    screenWidth: Int
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        tonalElevation = 0.dp,
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            // TODO: Check math on this later, or use intrinsic measurements
            .padding(start = (screenWidth / 2 - 68).dp, end = 0.dp)) {
            Row(modifier = Modifier
                .align(Alignment.CenterEnd)
            ) {
                NavigationBarItem(// Add Event Button
                    selected = selected == 0,
                    onClick = { onFirstClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = firstIcon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(120.dp),
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Unspecified,
                        unselectedIconColor = Color.Unspecified,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem( // Friends Button
                    selected = selected == 1,
                    onClick = { onSecondClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = secondIcon),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Unspecified,
                        unselectedIconColor = Color.Unspecified,
                        indicatorColor = Color.Transparent
                    )
                )
            } // Item Row
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
            onSecondClick = {},
            screenWidth = LocalConfiguration.current.screenWidthDp
        )
    }
}