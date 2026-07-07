package com.jksalcedo.tend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jksalcedo.tend.ui.add.AddPersonScreen
import com.jksalcedo.tend.ui.archived.ArchivedScreen
import com.jksalcedo.tend.ui.detail.PersonDetailScreen
import com.jksalcedo.tend.ui.home.HomeScreen
import com.jksalcedo.tend.ui.importcontacts.ImportContactsScreen

object Routes {
    const val HOME = "home"
    const val ADD_PERSON = "add_person?sharedData={sharedData}"
    const val DETAIL = "detail/{personId}"
    const val EDIT = "edit/{personId}"
    const val ARCHIVED = "archived"
    const val IMPORT_CONTACTS = "import_contacts"
}

@Composable
fun TendNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME,
    onOpenNotificationSettings: () -> Unit = {}
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
                onPersonClick = { personId -> navController.navigate("detail/$personId") },
                onOpenNotificationSettings = onOpenNotificationSettings,
                onArchivedClick = { navController.navigate(Routes.ARCHIVED) },
                onImportContactsClick = { navController.navigate(Routes.IMPORT_CONTACTS) }
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
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { personId -> navController.navigate("edit/$personId") }
            )
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("personId") {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId")
            AddPersonScreen(
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ARCHIVED) {
            ArchivedScreen(
                onPersonClick = { personId -> navController.navigate("detail/$personId") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.IMPORT_CONTACTS) {
            ImportContactsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
