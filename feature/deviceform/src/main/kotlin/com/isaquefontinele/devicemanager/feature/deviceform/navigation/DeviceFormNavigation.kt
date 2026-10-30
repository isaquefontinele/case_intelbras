package com.isaquefontinele.devicemanager.feature.deviceform.navigation

import kotlinx.serialization.Serializable

/** Opens the form to create a device, or to edit [deviceId] when it is set. */
@Serializable
data class DeviceFormDestination(val deviceId: String? = null)
