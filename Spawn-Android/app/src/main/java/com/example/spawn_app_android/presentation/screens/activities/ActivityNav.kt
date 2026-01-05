package com.example.spawn_app_android.presentation.screens.activities

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.spawn_app_android.presentation.screens.activities.flow.ActivitiesScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.ConfirmActivityScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityLocation
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityTimeScreen

object ActivityRoutes {
    const val STEP1 = "step1"
    const val STEP2 = "step2"
    const val STEP3 = "step3"
    const val STEP4 = "step4"
}

fun NavGraphBuilder.createActivityNavGraph(
    navController: NavHostController, activityViewModel: ActivityViewModel
) {
    // TODO: Need to setup Dependecy Injection with Hilt
    composable(ActivityRoutes.STEP1) {
//        val viewModel: ActivityViewModel = hiltViewModel()
        ActivitiesScreen(
            onNext = { navController.navigate(ActivityRoutes.STEP2) }, activityViewModel = activityViewModel
        )
    }

    composable(ActivityRoutes.STEP2) {
        SetActivityTimeScreen(activityViewModel = activityViewModel,
            onNext = { navController.navigate(ActivityRoutes.STEP3) },
            onBack = { navController.popBackStack() })
    }
    composable(ActivityRoutes.STEP3) {
        SetActivityLocation(activityViewModel = activityViewModel,
            onNext = { navController.navigate(ActivityRoutes.STEP4) },
            onBack = { navController.popBackStack() })
    }
    composable(ActivityRoutes.STEP4) {
        ConfirmActivityScreen(
            activityViewModel = activityViewModel,
            onConfirm = { navController.navigate(ActivityRoutes.STEP1) },
            onBack = { navController.popBackStack() }
        )
    }
}
