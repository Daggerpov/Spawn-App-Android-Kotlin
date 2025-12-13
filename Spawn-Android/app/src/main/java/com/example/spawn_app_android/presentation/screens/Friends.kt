package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.domain.model.Friend
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import com.example.spawn_app_android.presentation.theme.backgroundPrimaryDark
import com.example.spawn_app_android.presentation.theme.spawnIndigo
import com.example.spawn_app_android.presentation.theme.white

private val horizontalPadding = 26.dp

@Composable
fun FriendsPage(
    onFriendRequestsClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Friend Requests Ingress
        FriendRequestsIngress(onClick = onFriendRequestsClick)

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        FriendsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Your Friends Section
        YourFriendsSection(
            friends = sampleFriends,
            onFriendOptionsClick = { /* Handle options click */ }
        )
    }
}

private val sampleFriends = listOf(
    Friend("Daniel", "Lee", "@daniellee"),
    Friend("Sarah", "Chen", "@sarahc"),
    Friend("Marcus", "Johnson", "@marcusj"),
    Friend("Emily", "Zhang", "@emilyzhang")
)

@Composable
fun FriendRequestsIngress(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = spawnIndigo,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Friend Requests",
            fontSize = 16.sp,
            color = white
        )

        Text(
            text = "View All >",
            fontSize = 14.sp,
            color = white
        )
    }
}

@Composable
fun FriendsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF3E3B3B),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = white,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(white),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search for friends...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun YourFriendsSection(
    friends: List<Friend>,
    onFriendOptionsClick: (Friend) -> Unit
) {
    Column {
        Text(
            text = "Your Friends",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = white
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendListItem(
                    friend = friend,
                    onOptionsClick = { onFriendOptionsClick(friend) }
                )
            }
        }
    }
}

@Composable
fun FriendListItem(
    friend: Friend,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Profile Picture
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF5A5A5A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Username
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${friend.firstName} ${friend.lastName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = white
            )
            Text(
                text = friend.username,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // 3 Dots Menu
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = Color.Gray,
            modifier = Modifier
                .size(24.dp)
                .clickable { onOptionsClick() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Friends Page")
@Composable
fun FriendsPagePreview() {
    SpawnAppAndroidTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundPrimaryDark)
        ) {
            FriendsPage()
        }
    }
}