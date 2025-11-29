package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme

// preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsPage() {

    val friends = listOf("Daniel Lee", "Daniel Zhang", "Raphael Lee", "Jenson Huang")
    val recents = listOf("Steve Jobs", "John Grey", "Lucas Gingera")

    SpawnAppAndroidTheme {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            VerticalSpacer(60)
            FriendRequestTile(5)
            VerticalSpacer(60)
            //SearchBar() { }
            FriendsList(friends = friends)

        }
    }
}

@Composable
fun FriendRequestTile(numRequests: Int) {
    Spacer(modifier = Modifier)
    Row(
        modifier = Modifier
            .background(color = Color(red = 107, green = 129, blue = 251),
                shape = RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .height(45.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Friend Requests",
            fontSize = 16.sp,
            color = Color(255, 255, 255)
        )

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .width(20.dp)
                .height(20.dp)
                .background(color = Color(red = 255, green = 114, blue = 112),
                    shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center

        ) {
            Text(
                text = numRequests.toString(),
                fontSize = 12.sp,
                color = Color(255, 255, 255)
            )
        }
    }
}

@Composable
fun FriendsList (friends: List<String>) {

    Column() {
        for (friend in friends) {
            FriendTile(friend)
        }
    }

}

@Composable
fun FriendTile(name: String) {
    Row() {
        Text(
            text = name,
            fontSize = 14.sp,
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


//@Preview(showBackground = true,
//    showSystemUi = true,
//    name = "Friends")
//@Composable
//fun PreviewEventPage() {
//    FriendsPage()
//}