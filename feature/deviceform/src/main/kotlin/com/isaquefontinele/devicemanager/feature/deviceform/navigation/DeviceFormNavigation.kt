package com.isaquefontinele.devicemanager.feature.deviceform.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.isaquefontinele.devicemanager.feature.deviceform.DeviceFormResult
import com.isaquefontinele.devicemanager.feature.deviceform.DeviceFormRoute
import kotlinx.serialization.Serializable

/** Form to create or edit a device */
@Serializable
data class DeviceFormDestination(val deviceId: String? = null)

fun NavController.navigateToDeviceForm(deviceId: String? = null) = navigate(DeviceFormDestination(deviceId))

fun NavGraphBuilder.deviceFormScreen(onClose: (DeviceFormResult?) -> Unit) {
    composable<DeviceFormDestination> {
        DeviceFormRoute(onClose = onClose)
    }
}
