package com.isaquefontinele.devicemanager.feature.dashboard

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.MacAddress
import com.isaquefontinele.devicemanager.core.model.VideoDevice

internal fun Device.matches(filter: DeviceFilter): Boolean = when (filter) {
    DeviceFilter.ALL -> true
    DeviceFilter.FAVORITES -> isFavorite
    DeviceFilter.CAMERAS -> this is VideoDevice
    DeviceFilter.ALARMS -> this is AlarmCentral
}

/**
 * Case-insensitive search by name and serial number or MAC address. The MAC matches with or without
 * separators (`a0:b1` finds `A0B1C2D3E4F5`).
 */
internal fun Device.matches(query: String): Boolean {
    val text = query.trim()
    return text.isEmpty() || name.contains(text, ignoreCase = true) || identifierMatches(text)
}

private fun Device.identifierMatches(text: String): Boolean = when (this) {
    is VideoDevice -> serial.contains(text, ignoreCase = true)
    is AlarmCentral -> MacAddress.normalize(text).let { it.isNotEmpty() && macAddress.contains(it) }
}
