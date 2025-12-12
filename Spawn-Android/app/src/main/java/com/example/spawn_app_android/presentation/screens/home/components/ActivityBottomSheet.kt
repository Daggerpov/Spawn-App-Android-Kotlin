package com.example.spawn_app_android.presentation.screens.home.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.domain.model.ActivityModel
import com.example.spawn_app_android.presentation.screens.home.SpawnBodyTxt
import com.example.spawn_app_android.presentation.screens.home.SpawnTitleTxt
import com.example.spawn_app_android.presentation.screens.home.components.ActivityBottomSheet.SPACING_16
import com.example.spawn_app_android.presentation.screens.home.components.ActivityBottomSheet.SPAWN_IN_BUTTON_WIDTH
import com.example.spawn_app_android.presentation.theme.spawnIndigo
import com.example.spawn_app_android.presentation.theme.white
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityBottomSheet(
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    activity: ActivityModel,
    onDismiss: () -> Unit
){
    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = colorResource(R.color.activity_indigo)
    ) {
        Column(
            Modifier.padding(
                top = 0.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            )
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Start),
                painter = painterResource(R.drawable.ic_expand),
                contentDescription = "expand",
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(SPACING_16))

            SpawnTitleTxt(content = activity.title, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            SpawnBodyTxt(content = "${activity.status} • ${activity.time}", color = Color.White)

            Spacer(modifier = Modifier.height(SPACING_16))

            SpawnButton(
                modifier = Modifier.width(SPAWN_IN_BUTTON_WIDTH),
                iconRes = R.drawable.ic_spawn_button,
                buttonText = "Spawn In!",
                txtColor = spawnIndigo,
                bgColor = white,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(SPACING_16))

            MiniMapCard(
                locationName = "Golden Gate Park, San Francisco",
                latitude = 37.7694,
                longitude = -122.4862,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(SPACING_16))

            ChatRoomIngress(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChatRoomIngress(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.users_03),
                contentDescription = "users",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chatroom",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Haley: Come grab dinner with us...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Green, CircleShape)
            )
        }
    }
}

@Composable
fun MapCard() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ActivityBottomSheetPreview() {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val activity = ActivityModel()

    ActivityBottomSheet(
        sheetState = sheetState,
        coroutineScope = coroutineScope,
        activity = activity,
        onDismiss = {}
    )
}

object ActivityBottomSheet {
    val SPAWN_IN_BUTTON_WIDTH = 200.dp
    val SPACING_16 = 16.dp
}
