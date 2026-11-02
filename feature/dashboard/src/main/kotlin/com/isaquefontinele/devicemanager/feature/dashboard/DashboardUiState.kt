package com.isaquefontinele.devicemanager.feature.dashboard

import com.isaquefontinele.devicemanager.core.model.Device

enum class DeviceFilter { ALL, FAVORITES, CAMERAS, ALARMS }

data class DashboardUiState(
    val query: String = "",
    val filter: DeviceFilter = DeviceFilter.ALL,
    val totalDevicesCount: Int = 0,
    val content: DashboardContent = DashboardContent.Loading,
    val isRefreshing: Boolean = false,
    val selectedDevice: Device? = null,
    val removalDeviceCandidate: Device? = null,
)

sealed interface DashboardContent {
    data object Loading : DashboardContent
    data class Error(val reason: ErrorReason) : DashboardContent
    data object Empty : DashboardContent
    data object NoResults : DashboardContent
    data class Devices(val devices: List<Device>) : DashboardContent
}

enum class ErrorReason { NO_CONNECTION, UNAUTHORIZED, UNEXPECTED }

sealed interface DashboardMessage {
    data class RefreshFailed(val reason: ErrorReason) : DashboardMessage
    data object DeviceRemoved : DashboardMessage
    data object DeviceRemovedOffline : DashboardMessage
    data class RemoveFailed(val reason: ErrorReason) : DashboardMessage
    data class FavoriteChanged(val isFavorite: Boolean) : DashboardMessage
    data class ChangeRejected(val deviceName: String) : DashboardMessage
}
