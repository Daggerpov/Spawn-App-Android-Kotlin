package com.example.spawn_app_android.presentation.screens.activities

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun NavGraphBuilder.createEventNavGraph(
    navController: NavHostController
) {
    // TODO: Need to setup Dependecy Injection with Hilt
//    composable("step1") {
//        val viewModel: CreateEventViewModel = hiltViewModel()
//        Screen1SetActivityTime(
//            state = viewModel.state,
//            onEvent = viewModel::onEvent,
//            onNext = { navController.navigate("step2") }
//        )
//    }

    composable("step2") { /* similar */ }
    composable("step3") { /* similar */ }
    composable("step4") { /* final submit + summary */ }
}
