package com.isaquefontinele.devicemanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.DashboardDestination
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.DashboardNotice
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.dashboardScreen
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.sendDashboardNotice
import com.isaquefontinele.devicemanager.feature.deviceform.DeviceFormResult
import com.isaquefontinele.devicemanager.feature.deviceform.navigation.DeviceFormDestination
import com.isaquefontinele.devicemanager.feature.deviceform.navigation.deviceFormScreen
import com.isaquefontinele.devicemanager.feature.deviceform.navigation.navigateToDeviceForm

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DashboardDestination, modifier = modifier) {
        // The info screen is added in the next step; until then "Informações" does nothing.
        dashboardScreen(
            onAddDevice = { navController.navigateToDeviceForm() },
            onDeviceInfo = {},
            onEditDevice = { device -> navController.navigateToDeviceForm(device.id) },
        )
        deviceFormScreen(
            onClose = { result ->
                // A double tap on "X" or "Salvar" must not pop the dashboard as well.
                if (navController.currentBackStackEntry?.destination?.hasRoute<DeviceFormDestination>() == true) {
                    result?.let { navController.sendDashboardNotice(it.toDashboardNotice()) }
                    navController.popBackStack()
                }
            },
        )
    }
}

private fun DeviceFormResult.toDashboardNotice(): DashboardNotice = when (this) {
    DeviceFormResult.ADDED -> DashboardNotice.DEVICE_ADDED
    DeviceFormResult.UPDATED -> DashboardNotice.DEVICE_UPDATED
    DeviceFormResult.NOT_FOUND -> DashboardNotice.DEVICE_NOT_FOUND
}
