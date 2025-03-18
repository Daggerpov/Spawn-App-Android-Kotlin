import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.spawn_app_android.R
import com.example.spawn_app_android.ui.navigation.BottomNavigation
import com.example.spawn_app_android.ui.screens.EventsPage
import com.example.spawn_app_android.ui.screens.MapPage
import com.example.spawn_app_android.ui.theme.SpawnAppAndroidTheme
import kotlinx.serialization.Serializable

//@Serializable
//object EventsPage
//@Serializable
//object MapPage
//
//@Composable
//fun Navigation() {
//
//    val navController = rememberNavController()
//
//
//
//    NavHost(navController = navController, startDestination = "EventsPage") {
//        composable<EventsPage> { EventsPage }
//        composable<MapPage> { MapPage }
//    }
//
//}
@Serializable
data class Profile(val name: String)

@Serializable
object FriendsList

// Define the ProfileScreen composable.
@Composable
fun ProfileScreen(
    profile: Profile,
    onNavigateToFriendsList: () -> Unit,
) {
    Text("Profile for ${profile.name} :0:0:0SDFasdfasdfasdfasdfasdfasdfasdfasdf")
    Button(onClick = { onNavigateToFriendsList() }) {
        Text("Go to Friends List")
    }
}

// Define the FriendsListScreen composable.
@Composable
fun FriendsListScreen(onNavigateToProfile: () -> Unit) {
    Text("Friends List")
    Button(onClick = { onNavigateToProfile() }) {
        Text("Go to Profile")
    }
}

// Define the MyApp composable, including the `NavController` and `NavHost`.
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Profile(name = "John Smith")) {
        composable<Profile> { backStackEntry ->
            val profile: Profile = backStackEntry.toRoute()
            ProfileScreen(
                profile = profile,
                onNavigateToFriendsList = {
                    navController.navigate(route = FriendsList)
                }
            )
        }
        composable<FriendsList> {
            FriendsListScreen(
                onNavigateToProfile = {
                    navController.navigate(
                        route = Profile(name = "Aisha Devi")
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun NavigationPreview() {
    SpawnAppAndroidTheme {
        MyApp()
    }
}