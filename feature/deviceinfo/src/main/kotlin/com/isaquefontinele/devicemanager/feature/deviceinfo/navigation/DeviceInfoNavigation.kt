package com.isaquefontinele.devicemanager.feature.deviceinfo.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.isaquefontinele.devicemanager.feature.deviceinfo.DeviceInfoRoute
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoDestination(val deviceId: String)

fun NavController.navigateToDeviceInfo(deviceId: String) = navigate(DeviceInfoDestination(deviceId))

fun NavGraphBuilder.deviceInfoScreen(onBack: () -> Unit) {
    composable<DeviceInfoDestination> {
        DeviceInfoRoute(onBack = dropUnlessResumed { onBack() })
    }
}
