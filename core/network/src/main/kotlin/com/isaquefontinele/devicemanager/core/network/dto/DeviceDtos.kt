package com.isaquefontinele.devicemanager.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListResponseDto<T>(val count: Int, val data: List<T>)

@Serializable
data class AlarmCentralDto(val id: String, val name: String, val macAddress: String, val password: String)

@Serializable
data class VideoDeviceDto(
    val id: String,
    val name: String,
    val serial: String,
    val username: String,
    val password: String,
)

@Serializable
data class AlarmCentralRequestDto(val name: String, val macAddress: String, val password: String)

@Serializable
data class VideoDeviceRequestDto(val name: String, val serial: String, val username: String, val password: String)
