package com.isaquefontinele.devicemanager.core.database.entity

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
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

fun DeviceInput.toEntity(id: String, syncState: SyncState): DeviceEntity = when (this) {
    is DeviceInput.Alarm -> DeviceEntity(
        id = id,
        type = DeviceType.ALARM,
        name = name,
        password = password,
        macAddress = macAddress,
        syncState = syncState,
    )
    is DeviceInput.Video -> DeviceEntity(
        id = id,
        type = DeviceType.VIDEO,
        name = name,
        password = password,
        serial = serial,
        username = username,
        syncState = syncState,
    )
}

fun DeviceEntity.toInput(): DeviceInput = when (type) {
    DeviceType.ALARM -> DeviceInput.Alarm(name = name, macAddress = macAddress.orEmpty(), password = password)
    DeviceType.VIDEO -> DeviceInput.Video(
        name = name,
        serial = serial.orEmpty(),
        username = username.orEmpty(),
        password = password,
    )
}

fun DeviceWithFavorite.toDomain(): Device = when (device.type) {
    DeviceType.ALARM -> AlarmCentral(
        id = device.id,
        name = device.name,
        macAddress = device.macAddress.orEmpty(),
        password = device.password,
        isFavorite = isFavorite,
        isPendingSync = device.syncState != SyncState.SYNCED,
    )
    DeviceType.VIDEO -> VideoDevice(
        id = device.id,
        name = device.name,
        serial = device.serial.orEmpty(),
        username = device.username.orEmpty(),
        password = device.password,
        isFavorite = isFavorite,
        isPendingSync = device.syncState != SyncState.SYNCED,
    )
}
