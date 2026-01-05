package com.example.spawn_app_android.presentation.screens.activities.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.Utils.getNotifBarPadding
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel
import com.example.spawn_app_android.presentation.screens.activities.CreateActivityEvent

@Composable
fun ActivitiesScreen(
    onNext: () -> Unit,
    activityViewModel: ActivityViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = getNotifBarPadding() + 35.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.activities_header),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(35.dp))
        CategoriesCluster(onNext, activityViewModel)
        Spacer(Modifier.height(21.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.activity_edit),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CategoriesCluster(onNext: () -> Unit, activityViewModel: ActivityViewModel) {
    val categories = listOf("Eat", "Gym", "Study", "Chill", "Hike")
    FlowRow(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxLines = 2
    ) {
        categories.forEach { category ->
            CategoryCard(
                cat = category,
                icon = getIcon(category),
                onNext = onNext,
                activityViewModel = activityViewModel
            )
        }

        Column(
            modifier = Modifier
                .size(116.dp, 116.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(R.color.secondary_bg)),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_eat),
                contentDescription = "create new activity",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Create New Activity",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }

    }
}

@Composable
private fun CategoryCard(
    cat: String,
    icon: Int,
    onNext: () -> Unit,
    activityViewModel: ActivityViewModel
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .size(116.dp, 116.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.secondary_bg))
            .clickable(
                onClick = {
                    activityViewModel.onEvent(CreateActivityEvent.TagChanged(cat))
                    onNext()
                },
                interactionSource = interactionSource,
                indication = ripple()
            ),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = cat,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = cat, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = cat, style = MaterialTheme.typography.labelSmall)
    }
}

private fun getIcon(cat: String): Int {
    return when (cat.lowercase()) {
        "eat" -> R.drawable.ic_eat
        "gym" -> R.drawable.ic_gym
        "study" -> R.drawable.ic_pencil
        "chill" -> R.drawable.ic_chill
        "hike" -> R.drawable.ic_hike
        else -> R.drawable.ic_eat
    }
}

@Preview
@Composable
fun ActivitiesPreview() {
}