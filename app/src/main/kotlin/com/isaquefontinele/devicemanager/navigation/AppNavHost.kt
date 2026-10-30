package com.isaquefontinele.devicemanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.DashboardDestination
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.dashboardScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DashboardDestination, modifier = modifier) {
        //TODO Add actions for the dashboard screen
        dashboardScreen(
            onAddDevice = {},
            onDeviceInfo = {},
            onEditDevice = {},
        )
    }
}
