package com.isaquefontinele.devicemanager.core.model

/**
 * A device managed by the app. [isFavorite] is a local-only flag: it is never sent to the API.
 */
sealed interface Device {
    val id: String
    val name: String
    val password: String
    val isFavorite: Boolean
    val type: DeviceType
}

data class AlarmCentral(
    override val id: String,
    override val name: String,
    /** Normalized MAC address: 12 uppercase hex digits, without separators (API format). */
    val macAddress: String,
    override val password: String,
    override val isFavorite: Boolean = false,
) : Device {
    override val type: DeviceType get() = DeviceType.ALARM
}

data class VideoDevice(
    override val id: String,
    override val name: String,
    val serial: String,
    val username: String,
    override val password: String,
    override val isFavorite: Boolean = false,
) : Device {
    override val type: DeviceType get() = DeviceType.VIDEO
}
