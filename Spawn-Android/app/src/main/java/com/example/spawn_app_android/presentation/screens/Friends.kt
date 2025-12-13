package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.spawn_app_android.presentation.theme.spawnIndigo
import com.example.spawn_app_android.presentation.theme.textPrimary
import com.example.spawn_app_android.presentation.theme.textSecondary
import com.example.spawn_app_android.presentation.theme.white

/**
 * Friends.kt
 *
 * Created by Ethan Dsouza on 2025-12-13
 *
 * Provides start to Friends Tab flow from the main app
 */

private val horizontalPadding = 26.dp

enum class AddButtonState {
    ADD,
    PENDING,
    ADDED
}

data class RecentUser(
    val friend: Friend,
    val addState: AddButtonState = AddButtonState.ADD
)

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

        Spacer(modifier = Modifier.height(24.dp))

        // Recently Spawned With Section
        RecentlySpawnedWithSection(
            recentUsers = sampleRecentUsers,
            onAddClick = { /* Handle add click */ }
        )
    }
}

private val sampleFriends = listOf(
    Friend("Daniel", "Lee", "@daniellee"),
    Friend("Sarah", "Chen", "@sarahc"),
    Friend("Marcus", "Johnson", "@marcusj"),
    Friend("Emily", "Zhang", "@emilyzhang")
)

private val sampleRecentUsers = listOf(
    RecentUser(Friend("Alex", "Kim", "@alexkim"), AddButtonState.ADD),
    RecentUser(Friend("Jordan", "Smith", "@jordans"), AddButtonState.PENDING),
    RecentUser(Friend("Taylor", "Brown", "@taylorbrown"), AddButtonState.ADDED)
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
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = SolidColor(textPrimary),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = textPrimary,
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
            cursorBrush = SolidColor(textPrimary),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search for friends...",
                        color = textPrimary,
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
            color = textSecondary
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
                color = textSecondary
            )
            Text(
                text = friend.username,
                fontSize = 14.sp,
                color = textSecondary
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

@Composable
fun RecentlySpawnedWithSection(
    recentUsers: List<RecentUser>,
    onAddClick: (RecentUser) -> Unit
) {
    Column {
        Text(
            text = "Recently Spawned With",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            recentUsers.forEach { recentUser ->
                RecentlySpawnedItem(
                    recentUser = recentUser,
                    onAddClick = { onAddClick(recentUser) }
                )
            }
        }
    }
}

@Composable
fun RecentlySpawnedItem(
    recentUser: RecentUser,
    onAddClick: () -> Unit
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
                text = "${recentUser.friend.firstName} ${recentUser.friend.lastName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary
            )
            Text(
                text = recentUser.friend.username,
                fontSize = 14.sp,
                color = textSecondary
            )
        }

        // Add Button
        AddFriendButton(
            state = recentUser.addState,
            onClick = onAddClick
        )
    }
}

@Composable
fun AddFriendButton(
    state: AddButtonState,
    onClick: () -> Unit
) {
    val text: String
    val backgroundColor: Color
    val textColor: Color
    val isClickable: Boolean

    when (state) {
        AddButtonState.ADD -> {
            text = "Add"
            backgroundColor = spawnIndigo
            textColor = white
            isClickable = true
        }
        AddButtonState.PENDING -> {
            text = "Pending"
            backgroundColor = Color(0xFF3E3B3B)
            textColor = Color.Gray
            isClickable = false
        }
        AddButtonState.ADDED -> {
            text = "Added"
            backgroundColor = Color(0xFF2E7D32)
            textColor = white
            isClickable = false
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isClickable) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Preview(showBackground = true, name = "Friends Page")
@Composable
fun FriendsPagePreview() {
    SpawnAppAndroidTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FriendsPage()
        }
    }
}