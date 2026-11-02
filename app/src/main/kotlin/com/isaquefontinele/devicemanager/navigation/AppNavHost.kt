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
import com.isaquefontinele.devicemanager.feature.deviceinfo.navigation.deviceInfoScreen
import com.isaquefontinele.devicemanager.feature.deviceinfo.navigation.navigateToDeviceInfo

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DashboardDestination, modifier = modifier) {
        dashboardScreen(
            onAddDevice = { navController.navigateToDeviceForm() },
            onDeviceInfo = { device -> navController.navigateToDeviceInfo(device.id) },
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
        deviceInfoScreen(onBack = { navController.popBackStack() })
    }
}

private fun DeviceFormResult.toDashboardNotice(): DashboardNotice = when (this) {
    DeviceFormResult.ADDED -> DashboardNotice.DEVICE_ADDED
    DeviceFormResult.UPDATED -> DashboardNotice.DEVICE_UPDATED
    DeviceFormResult.SAVED_OFFLINE -> DashboardNotice.DEVICE_SAVED_OFFLINE
    DeviceFormResult.NOT_FOUND -> DashboardNotice.DEVICE_NOT_FOUND
}
