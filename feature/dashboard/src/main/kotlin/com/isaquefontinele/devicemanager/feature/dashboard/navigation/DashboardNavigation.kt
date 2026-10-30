package com.isaquefontinele.devicemanager.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.feature.dashboard.DashboardRoute
import kotlinx.serialization.Serializable

@Serializable
data object DashboardDestination

fun NavGraphBuilder.dashboardScreen(
    onAddDevice: () -> Unit,
    onDeviceInfo: (Device) -> Unit,
    onEditDevice: (Device) -> Unit,
) {
    composable<DashboardDestination> {
        DashboardRoute(onAddDevice = onAddDevice, onDeviceInfo = onDeviceInfo, onEditDevice = onEditDevice)
    }
}
