package com.example.spawn_app_android.presentation.screens

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.navigation.FriendsList
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.mapbox.maps.extension.style.expressions.dsl.generated.all
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod

// preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage() {

    val image = painterResource(R.drawable.daniel_lee)
    val name = "Daniel Lee"
    val tag = "daniellee"
    val hobbies = listOf("Poker", "League", "Leetcode", "Raving", "Basketball")
    val stats = mapOf("People Met" to 49, "Spawns Made" to 4, "Spawns Joined" to 16)

    SpawnAppAndroidTheme {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            VerticalSpacer(60)
            ProfileHead(image, name, tag)
            VerticalSpacer(60)
            HobbiesBlock(hobbies)
            VerticalSpacer(60)
            StatsBlock(stats)

        }
    }
}

// takes in a reduced user object with only pfp, name
@Composable
fun ProfileHead(image: Painter, name: String, tag: String) {

    Column {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier // Use a new Modifier instance here
                .size(72.dp)
                .clip(CircleShape)
        )

        Text(
            text = name
        )

        Text(
            text = "@" + tag
        )

    }

}

@Composable
fun HobbiesBlock(hobbies: List<String>) {

    Column() {

        for (hobby in hobbies) {
            Text(hobby)
        }

    }

}

@Composable
fun StatsBlock(stats: Map<String, Int>) {

    Row {
        for (entry in stats) {
            StatComponent(entry.key, entry.value)
        }
    }

}

@Composable
fun StatComponent(statName: String, stat: Int) {
    Column {
        Text(text = stat.toString())
        Text(text = statName)
    }
}

@Composable
fun Calendar() {

}

@Preview(showBackground = true,
    showSystemUi = true,
    name = "Profile")
@Composable
fun PreviewEventPage() {
    ProfilePage()
}