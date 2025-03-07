package com.example.spawn_app_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.ui.theme.SpawnAppAndroidTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.clip
import com.example.spawn_app_android.ui.screens.MapPage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpawnAppAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        HelloCard("Daniel Lee", modifier = Modifier.padding(innerPadding))
                        FriendFilterReel(filters = arrayOf("Everyone", "Close Friends", "Sports", "Hobbies", "Studying"))
                        EventsReel(events = listOf("Dinner time!!!!!", "Basketball run", "Painting sesh!", "Light 5k run", "Painting sesh!"))
                    }
                }
            }
        }
    }
}

@Composable
fun MapToggle(modifier: Modifier = Modifier) {
    Text(
        text = "Map Toggle",
        modifier = Modifier.padding(16.dp)
    )
}


@Composable
fun HelloCard(name: String, modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.daniel_lee)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp), // Modifier must come first
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = stringResource(R.string.hello),
                fontSize = 30.sp
            )
            Text(
                text = "★$name",
                fontSize = 38.sp
            )
        }

        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier // Use a new Modifier instance here
                .size(72.dp)
                .clip(CircleShape)

        )
    }
}

@Composable
fun FriendFilterReel(filters: Array<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()), // Enables horizontal scrolling

        horizontalArrangement = Arrangement.Start
    ) {
        for (filter in filters) {
            Text(
                text = filter,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp) // Space between buttons
                    .background(
                        color = Color.LightGray,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp) // Inner padding for button size
            )
        }
    }
}

// TODO: Create scrollable reel of clickable events
@Composable
fun EventsReel(events: List<String>, modifier: Modifier = Modifier) {
    LazyColumn( // LazyColumn makes it scrollable vertically
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        items(events.size) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ensures each box takes the full width
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = Color(0xFF8693FF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp) // Inner padding
            ) {
                Column {
                    CompositionLocalProvider(LocalContentColor provides Color.White) {
                        Text(
                            text = events[index],
                            fontSize = 22.sp,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                        )
                        Text(
                            text = "10:00 PM - 11:30 PM",
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Text(
                            text = "Gather - Place Vanier",
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)

                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true,
    showSystemUi = false,
    name = "Dashboard")
@Composable
fun Preview() {
    SpawnAppAndroidTheme {
        Column {
            HelloCard("Daniel Lee")
            FriendFilterReel(filters = arrayOf("Everyone", "Close Friends", "Sports"))
            EventsReel(events = listOf("Dinner time!!!!!", "Basketball run", "Painting sesh!", "Light 5k run", "Painting sesh!") )
        }
    }
}