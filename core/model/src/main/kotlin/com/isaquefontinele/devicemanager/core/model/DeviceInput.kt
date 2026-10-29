package com.isaquefontinele.devicemanager.core.model

/** Data typed by the user to create or update a device, before validation. */
sealed interface DeviceInput {
    val name: String
    val password: String

    data class Alarm(override val name: String, val macAddress: String, override val password: String) : DeviceInput

    data class Video(
        override val name: String,
        val serial: String,
        val username: String,
        override val password: String,
    ) : DeviceInput
}
