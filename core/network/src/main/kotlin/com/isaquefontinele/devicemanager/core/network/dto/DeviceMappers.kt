package com.isaquefontinele.devicemanager.core.network.dto

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice

fun AlarmCentralDto.toDomain() = AlarmCentral(id = id, name = name, macAddress = macAddress, password = password)

fun VideoDeviceDto.toDomain() = VideoDevice(
    id = id,
    name = name,
    serial = serial,
    username = username,
    password = password,
)

fun DeviceInput.Alarm.toRequest() = AlarmCentralRequestDto(name = name, macAddress = macAddress, password = password)

fun DeviceInput.Video.toRequest() = VideoDeviceRequestDto(
    name = name,
    serial = serial,
    username = username,
    password = password,
)
