package com.example.spawn_app_android.presentation.screens.activities

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.spawn_app_android.presentation.screens.activities.ActivityRoutes
import com.example.spawn_app_android.presentation.screens.activities.flow.ActivitiesScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityTimeScreen

object ActivityRoutes {
    const val STEP1 = "step1"
    const val STEP2 = "step2"
    const val STEP3 = "step3"
    const val STEP4 = "step4"
}

fun NavGraphBuilder.createActivityNavGraph(
    navController: NavHostController,
    activityViewModel: ActivityViewModel
) {
    // TODO: Need to setup Dependecy Injection with Hilt
    composable(ActivityRoutes.STEP1) {
//        val viewModel: ActivityViewModel = hiltViewModel()
        ActivitiesScreen(
            onNext = { navController.navigate("step2") },
            activityViewModel = activityViewModel
        )
    }

    composable(ActivityRoutes.STEP2) {
        SetActivityTimeScreen(
            activityViewModel = activityViewModel,
            onNext = { navController.navigate("step3") },
            onBack = { navController.popBackStack() })
    }
    composable(ActivityRoutes.STEP3) { /* similar */ }
    composable(ActivityRoutes.STEP4) { /* final submit + summary */ }
}
