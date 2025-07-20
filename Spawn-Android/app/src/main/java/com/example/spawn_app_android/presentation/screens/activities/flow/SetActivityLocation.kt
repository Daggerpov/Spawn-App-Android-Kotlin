package com.example.spawn_app_android.presentation.screens.activities.flow

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.screens.activities.ActivityViewModel
import com.example.spawn_app_android.presentation.screens.activities.CreateActivityEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetActivityLocation(
    onNext: () -> Unit,
    activityViewModel: ActivityViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Trigger function from anywhere
    fun triggerBottomSheet() {
        showSheet = true
    }

    HeaderMain(onBack = onBack)
    LaunchedEffect(Unit) {
        triggerBottomSheet()
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    showSheet = false
                }
            },
            sheetState = sheetState,
            containerColor = colorResource(R.color.white)
        ) {
            HeaderBtmSheet(onBack)
            Spacer(Modifier.height(23.dp))
            SearchSection(activityViewModel)

        }
    }
}

@Composable
private fun SearchSection(activityViewModel: ActivityViewModel) {
    SearchBar(activityViewModel)
    SearchSuggestions()

}

@Composable
private fun SearchSuggestions() {
    LazyColumn() {  }
}

@Composable
private fun SearchBar(activityViewModel: ActivityViewModel) {
    TextField(
        stringResource(R.string.where_at),
        onValueChange = { activityViewModel.onEvent(CreateActivityEvent.LocationChanged(it)) },
        modifier = Modifier.padding(horizontal = 26.dp))
}

@Composable
private fun HeaderBtmSheet(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
    ) {
        BackButton(onClick = onBack, R.drawable.ic_back_black)

        Text(
            stringResource(R.string.choose_location),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun HeaderMain(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        BackButton(onClick = {
            Log.d("HeaderMain", "Back button clicked")
            onBack()
        }, R.drawable.ic_back)
        Spacer(Modifier.width(16.dp))
        Text(
            stringResource(R.string.location_dummy),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}