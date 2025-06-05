package com.example.spawn_app_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SpawnApp()
        }
    }
}

@Preview(showBackground = true,
    showSystemUi = false,
    name = "Dashboard")
@Composable
fun Preview() {
    SpawnApp()
}