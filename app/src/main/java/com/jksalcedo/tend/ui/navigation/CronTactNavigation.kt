package com.jksalcedo.tend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jksalcedo.tend.ui.add.AddPersonScreen
import com.jksalcedo.tend.ui.detail.PersonDetailScreen
import com.jksalcedo.tend.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_PERSON = "add_person?sharedData={sharedData}"
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
                onAddPersonClick = { sharedData ->
                    if (sharedData != null) {
                        navController.navigate("add_person?sharedData=$sharedData")
                    } else {
                        navController.navigate("add_person")
                    }
                },
                onPersonClick = { personId -> navController.navigate("detail/$personId") }
            )
        }
        composable(
            route = Routes.ADD_PERSON,
            arguments = listOf(navArgument("sharedData") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val sharedData = backStackEntry.arguments?.getString("sharedData")
            AddPersonScreen(
                sharedData = sharedData,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("personId") {
                type = NavType.LongType
            })
        ) {
            PersonDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
