package com.jksalcedo.tend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jksalcedo.tend.ui.add.AddPersonScreen
import com.jksalcedo.tend.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_PERSON = "add_person"
    const val DETAIL = "detail/{personId}"
}

@Composable
fun TendNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddPersonClick = { navController.navigate(Routes.ADD_PERSON) },
                onPersonClick = { personId -> navController.navigate("detail/$personId") }
            )
        }
        composable(Routes.ADD_PERSON) {
            AddPersonScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(androidx.navigation.navArgument("personId") { type = androidx.navigation.NavType.LongType })
        ) {
            com.jksalcedo.tend.ui.detail.PersonDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
