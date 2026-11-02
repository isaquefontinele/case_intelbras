package com.isaquefontinele.devicemanager.core.insights

data class DeviceInfo(val isCamera: Boolean, val isFavorite: Boolean)

data class DeviceSummary(val cameras: Int = 0, val alarms: Int = 0, val favorites: Int = 0)

fun summarize(devices: List<DeviceInfo>): DeviceSummary {
    val cameras = devices.count { it.isCamera }
    return DeviceSummary(
        cameras = cameras,
        alarms = devices.size - cameras,
        favorites = devices.count { it.isFavorite },
    )
}
