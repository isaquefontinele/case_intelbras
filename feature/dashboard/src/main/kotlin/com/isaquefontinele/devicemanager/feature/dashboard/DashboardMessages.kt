package com.isaquefontinele.devicemanager.feature.dashboard

import androidx.annotation.StringRes

@StringRes
internal fun ErrorReason.messageRes(): Int = when (this) {
    ErrorReason.NO_CONNECTION -> R.string.dashboard_error_no_connection
    ErrorReason.UNAUTHORIZED -> R.string.dashboard_error_unauthorized
    ErrorReason.UNEXPECTED -> R.string.dashboard_error_unexpected
}

@StringRes
internal fun DashboardMessage.messageRes(): Int = when (this) {
    is DashboardMessage.RefreshFailed -> when (reason) {
        ErrorReason.NO_CONNECTION -> R.string.dashboard_refresh_failed_no_connection
        ErrorReason.UNAUTHORIZED -> R.string.dashboard_error_unauthorized
        ErrorReason.UNEXPECTED -> R.string.dashboard_refresh_failed
    }
    DashboardMessage.DeviceRemoved -> R.string.dashboard_device_removed
    is DashboardMessage.RemoveFailed -> when (reason) {
        ErrorReason.UNAUTHORIZED -> R.string.dashboard_error_unauthorized
        else -> R.string.dashboard_remove_failed
    }
    is DashboardMessage.FavoriteChanged ->
        if (isFavorite) R.string.dashboard_favorite_added else R.string.dashboard_favorite_removed
}

@StringRes
internal fun DeviceFilter.labelRes(): Int = when (this) {
    DeviceFilter.ALL -> R.string.dashboard_filter_all
    DeviceFilter.FAVORITES -> R.string.dashboard_filter_favorites
    DeviceFilter.CAMERAS -> R.string.dashboard_filter_cameras
    DeviceFilter.ALARMS -> R.string.dashboard_filter_alarms
}
