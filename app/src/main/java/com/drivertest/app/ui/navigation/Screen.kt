package com.drivertest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Learn : Screen("learn", "学习", Icons.Default.School)
    data object Add : Screen("add", "添加", Icons.Default.AddCircle)
    data object Stats : Screen("stats", "统计", Icons.Default.BarChart)
    data object Settings : Screen("settings", "设置", Icons.Default.School) // icon unused - not in bottom bar

    companion object {
        val items = listOf(Learn, Add, Stats)
    }
}
