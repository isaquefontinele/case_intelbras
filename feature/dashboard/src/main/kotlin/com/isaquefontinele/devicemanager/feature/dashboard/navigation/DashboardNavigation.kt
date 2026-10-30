package com.isaquefontinele.devicemanager.feature.dashboard.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.feature.dashboard.DashboardRoute
import kotlinx.serialization.Serializable

@Serializable
data object DashboardDestination

enum class DashboardNotice { DEVICE_ADDED, DEVICE_UPDATED, DEVICE_NOT_FOUND }

private const val NOTICE_KEY = "dashboard_notice"

fun NavController.sendDashboardNotice(notice: DashboardNotice) {
    getBackStackEntry<DashboardDestination>().savedStateHandle[NOTICE_KEY] = notice.name
}

fun NavGraphBuilder.dashboardScreen(
    onAddDevice: () -> Unit,
    onDeviceInfo: (Device) -> Unit,
    onEditDevice: (Device) -> Unit,
) {
    composable<DashboardDestination> { entry ->
        val notice by entry.savedStateHandle.getStateFlow<String?>(NOTICE_KEY, null).collectAsStateWithLifecycle()
        DashboardRoute(
            onAddDevice = onAddDevice,
            onDeviceInfo = onDeviceInfo,
            onEditDevice = onEditDevice,
            notice = notice?.let(DashboardNotice::valueOf),
            onNoticeShown = { entry.savedStateHandle[NOTICE_KEY] = null },
        )
    }
}
