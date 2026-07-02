package com.drivertest.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drivertest.app.ui.add.AddScreen
import com.drivertest.app.ui.learn.LearnScreen
import com.drivertest.app.ui.settings.SettingsScreen
import com.drivertest.app.ui.stats.StatsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Learn.route
    ) {
        composable(Screen.Learn.route) {
            LearnScreen()
        }
        composable(Screen.Add.route) {
            AddScreen()
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
