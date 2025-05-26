package com.example.spawn_app_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spawn_app_android.presentation.screens.EventsPage
import com.example.spawn_app_android.presentation.screens.MapPage
import kotlinx.serialization.Serializable

@Serializable
object EventsPage
@Serializable
object MapPage

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            //EventsPage()
            App()
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = EventsPage) {
        composable<EventsPage> { EventsPage(onNavigateToMapPage = {navController.navigate(route = MapPage)}) }
        composable<MapPage> { MapPage(onNavigateToEventsPage = {navController.navigate(route = EventsPage)}) }
    }

}


@Preview(showBackground = true,
    showSystemUi = false,
    name = "Dashboard")
@Composable
fun Preview() {
    //EventsPage()
}