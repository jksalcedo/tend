package com.jksalcedo.crontact.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jksalcedo.crontact.ui.add.AddPersonScreen
import com.jksalcedo.crontact.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_PERSON = "add_person"
}

@Composable
fun CronTactNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddPersonClick = { navController.navigate(Routes.ADD_PERSON) }
            )
        }
        composable(Routes.ADD_PERSON) {
            AddPersonScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
