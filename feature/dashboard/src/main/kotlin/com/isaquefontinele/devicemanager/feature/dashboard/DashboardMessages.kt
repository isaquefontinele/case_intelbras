package com.isaquefontinele.devicemanager.feature.dashboard

import android.content.res.Resources
import androidx.annotation.StringRes
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.DashboardNotice

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
    DashboardMessage.DeviceRemovedOffline -> R.string.dashboard_device_removed_offline
    is DashboardMessage.RemoveFailed -> when (reason) {
        ErrorReason.UNAUTHORIZED -> R.string.dashboard_error_unauthorized
        else -> R.string.dashboard_remove_failed
    }
    is DashboardMessage.FavoriteChanged ->
        if (isFavorite) R.string.dashboard_favorite_added else R.string.dashboard_favorite_removed
    is DashboardMessage.ChangeRejected -> R.string.dashboard_change_rejected
}

internal fun DashboardMessage.text(resources: Resources): String = when (this) {
    is DashboardMessage.ChangeRejected -> resources.getString(messageRes(), deviceName)
    else -> resources.getString(messageRes())
}

@StringRes
internal fun DeviceFilter.labelRes(): Int = when (this) {
    DeviceFilter.ALL -> R.string.dashboard_filter_all
    DeviceFilter.FAVORITES -> R.string.dashboard_filter_favorites
    DeviceFilter.CAMERAS -> R.string.dashboard_filter_cameras
    DeviceFilter.ALARMS -> R.string.dashboard_filter_alarms
}

@StringRes
internal fun DashboardNotice.messageRes(): Int = when (this) {
    DashboardNotice.DEVICE_ADDED -> R.string.dashboard_device_added
    DashboardNotice.DEVICE_UPDATED -> R.string.dashboard_device_updated
    DashboardNotice.DEVICE_SAVED_OFFLINE -> R.string.dashboard_device_saved_offline
    DashboardNotice.DEVICE_NOT_FOUND -> R.string.dashboard_device_not_found
}
