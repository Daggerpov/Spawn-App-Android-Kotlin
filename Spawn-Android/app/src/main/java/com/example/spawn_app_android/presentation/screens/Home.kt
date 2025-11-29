package com.example.spawn_app_android.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.spawn_app_android.presentation.screens.Utils.SetDarkStatusBarIcons
import com.example.spawn_app_android.presentation.screens.components.ActivityBottomSheet
import com.example.spawn_app_android.presentation.viewModels.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun HomeScreenScrollable(viewModel: HomeViewModel = viewModel()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            HomeScreen(viewModel)
        }

    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    SetDarkStatusBarIcons()
//    val activities by viewModel.filteredActivities.collectAsState()
    val filters = listOf("Eat", "Gym", "Study", "Chill")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
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
            Text("Hey Daniel! 👋", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(filters = filters, onFilterSelected = viewModel::setFilter)

            Spacer(modifier = Modifier.height(30.dp))
            ActivitiesReel(viewModel)
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
fun ActivitiesReel(viewModel: HomeViewModel) {
    // will later be fetched using HomeViewModel
    val activities = listOf<ActivityModel>()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            "See what’s happening", style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.black_400)
        )
        Text(
            "See all", style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            color = colorResource(R.color.activity_indigo)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(viewModel.getActivities()) { activity ->
            ActivityCard(activity)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(activity: ActivityModel) {
    //region DECLARATIONS
    val coroutineScope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // Trigger function from anywhere
    fun triggerBottomSheet() {
        showSheet = true
    }
    //endregion

    //region ACTIVITY CARD CONTENT
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (activity.tag.lowercase()) {
                "eat" -> colorResource(R.color.activity_red)
                "gym" -> colorResource(R.color.activity_indigo)
                "study" -> colorResource(R.color.activity_purple)
                "chill" -> colorResource(R.color.activity_indigo_dark)
                else -> colorResource(R.color.black_400)
            }
        ),
        onClick = { triggerBottomSheet() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(activity.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "By ${activity.host} • ${activity.time}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp, 6.dp, 12.dp, 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_location),
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
        }

        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 8.dp))
                .padding(0.dp)
                .background(pickTagColor(activity.status))
                .padding(10.dp, 6.dp, 10.dp, 6.dp)
                .align(Alignment.End),
            text = activity.status,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight(600),
                color = colorResource(R.color.transparentBlack80),
            )
        )
        //endregion
        if (showSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ActivityBottomSheet(
                sheetState = sheetState,
                coroutineScope = coroutineScope,
                activity = activity,
                onDismiss = { showSheet = false }
            )
        }

    }
}

@Composable
fun SpawnTitleTxt(
    modifier: Modifier = Modifier,
    content: String,
    color: Color = Color.Black
) {
    Text(
        modifier = modifier,
        text = content,
        style = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight(600),
            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            color = color,
        )
    )
}

@Composable
fun SpawnBodyTxt(
    modifier: Modifier = Modifier,
    content: String,
    color: Color = Color.Black
) {
    Text(
        modifier = modifier,
        text = content,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight(600),
            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
            color = color,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetUI(coroutineScope: CoroutineScope, sheetState: SheetState) {
    Column(Modifier.padding(24.dp)) {
        Text("This is the bottom sheet content!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                sheetState.hide()
//                showSheet = false
            }
        }) {
            Text("Close")
        }
    }
}


@Composable
private fun pickTagColor(activityStatus: String): Color {
    if (activityStatus.lowercase() == "happening now") {
        return colorResource(R.color.icon_positive)
    } else {
        return Color.White
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
