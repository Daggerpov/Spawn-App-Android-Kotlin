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
import com.example.spawn_app_android.ui.screens.EventDashboard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpawnAppAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        val eventDashboard = EventDashboard()
                        eventDashboard.HelloCard("Daniel Lee", modifier = Modifier.padding(innerPadding))
                        eventDashboard.FriendFilterReel(filters = arrayOf("Everyone", "Close Friends", "Sports", "Hobbies", "Studying"))
                        eventDashboard.EventsReel(events = listOf("Dinner time!!!!!", "Basketball run", "Painting sesh!", "Light 5k run", "Painting sesh!"))
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
            val eventDashboard = EventDashboard()
            eventDashboard.HelloCard("Daniel Lee")
            eventDashboard.FriendFilterReel(filters = arrayOf("Everyone", "Close Friends", "Sports"))
            eventDashboard.EventsReel(events = listOf("Dinner time!!!!!", "Basketball run", "Painting sesh!", "Light 5k run", "Painting sesh!") )
        }
    }
}