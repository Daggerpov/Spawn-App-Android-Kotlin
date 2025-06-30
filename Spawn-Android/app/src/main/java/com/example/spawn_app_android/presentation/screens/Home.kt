package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spawn_app_android.R
import com.example.spawn_app_android.domain.model.ActivityModel
import com.example.spawn_app_android.presentation.viewModels.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
//    val activities by viewModel.filteredActivities.collectAsState()
    val filters = listOf("Eat", "Gym", "Study", "Chill")
    Column(
        modifier = Modifier
            .fillMaxWidth(),

        ) {
        Image(
            modifier = Modifier
                .padding(0.72903.dp)
                .width(78.75194.dp)
                .height(31.99965.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.spawn_logo),
            contentDescription = "spawn_logo"
        )

        Spacer(modifier = Modifier.height(38.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Hey Daniel! 👋", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(filters = filters, onFilterSelected = viewModel::setFilter)

            Spacer(modifier = Modifier.height(30.dp))
            ActivitiesReel()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
//            items(activities) { activity ->
//                ActivityCard(activity)
//            }
            }
        }
    }


}

@Composable
fun FilterRow(filters: List<String>, onFilterSelected: (String?) -> Unit) {

    Row(
        modifier = Modifier
            .padding(top = 38.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Spawn in!", color = colorResource(R.color.text_contrast), fontSize = 16.sp)
        Text(
            "See All",
            style = TextStyle(
                fontSize = 12.sp,
//                fontFamily = FontFamily(Font(R.font.onest)),
                fontWeight = FontWeight(600),
                color = colorResource(R.color.activity_indigo),
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        ImageCard(R.drawable.ic_eat, "Eat")
        ImageCard(R.drawable.ic_gym, "Gym")
        ImageCard(R.drawable.ic_pencil, "Study")
        ImageCard(R.drawable.ic_chill, "Chill")
    }
}

@Composable
fun ImageCard(iconId: Int, caption: String) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = "Sample Image",
                modifier = Modifier
                    .height(40.dp),
                contentScale = ContentScale.FillHeight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = caption,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivitiesReel() {
    // will later be fetched using HomeViewModel
    val activities = listOf<ActivityModel>()

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {

        Text(
            "See what’s happening", style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.black_400)
        )
        Text(
            "See all", style = MaterialTheme.typography.titleMedium,
            fontSize = 12.sp,
            color = colorResource(R.color.activity_indigo)
        )
    }

    val event = ActivityModel("1", "Late Night Ramen Run", "Alex Chen", "10:30 PM", "Downtown Ramen Bar", "2.1 km", "Eat", "HAPPENING NOW")

    ActivityCard(event)
}

@Composable
fun ActivityCard(activity: ActivityModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (activity.tag) {
                "Eat" -> colorResource(R.color.activity_red)
                "Gym" -> colorResource(R.color.activity_indigo)
                "Study" -> colorResource(R.color.activity_purple)
                "Chill" -> colorResource(R.color.activity_indigo_dark)
                else -> colorResource(R.color.black_400)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "By ${activity.host} • ${activity.time}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = "location",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${activity.location} • ${activity.distance}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(activity.status) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    labelColor = Color.White
                )
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

