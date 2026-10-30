package com.isaquefontinele.devicemanager.core.database.entity

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.VideoDevice

fun Device.toEntity(): DeviceEntity = when (this) {
    is AlarmCentral -> DeviceEntity(
        id = id,
        type = DeviceType.ALARM,
        name = name,
        password = password,
        macAddress = macAddress,
    )
    is VideoDevice -> DeviceEntity(
        id = id,
        type = DeviceType.VIDEO,
        name = name,
        password = password,
        serial = serial,
        username = username,
    )
}

fun DeviceWithFavorite.toDomain(): Device = when (device.type) {
    DeviceType.ALARM -> AlarmCentral(
        id = device.id,
        name = device.name,
        macAddress = device.macAddress.orEmpty(),
        password = device.password,
        isFavorite = isFavorite,
    )
    DeviceType.VIDEO -> VideoDevice(
        id = device.id,
        name = device.name,
        serial = device.serial.orEmpty(),
        username = device.username.orEmpty(),
        password = device.password,
        isFavorite = isFavorite,
    )
}
