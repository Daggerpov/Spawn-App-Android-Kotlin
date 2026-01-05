package com.example.spawn_app_android.presentation.screens.activities

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.spawn_app_android.presentation.screens.activities.flow.ActivitiesScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.ActivitySuccessScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.ConfirmActivityScreen
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityLocation
import com.example.spawn_app_android.presentation.screens.activities.flow.SetActivityTimeScreen
import com.example.spawn_app_android.presentation.navigation.BottomNavItem

object ActivityRoutes {
    const val STEP1 = "activitiesTab"
    const val STEP2 = "setActivityTimeScreen"
    const val STEP3 = "setActivityLocationScreen"
    const val STEP4 = "confirmActivityDetailsScreen"
    const val STEP5 = "activitySuccessScreen"
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
            onConfirm = { navController.navigate(ActivityRoutes.STEP5) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(ActivityRoutes.STEP5) {
        ActivitySuccessScreen(
            onDone = {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo(BottomNavItem.Home.route) { inclusive = true }
                }
            },
            activityViewModel = activityViewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
