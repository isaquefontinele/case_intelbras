package com.isaquefontinele.devicemanager.core.network

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice

/**
 * Remote source of devices. Every function throws [NetworkError] on failure.
 * Inputs are expected to be already validated and normalized.
 */
interface DeviceRemoteDataSource {
    suspend fun getAlarmCentrals(): List<AlarmCentral>
    suspend fun createAlarmCentral(input: DeviceInput.Alarm): AlarmCentral
    suspend fun updateAlarmCentral(id: String, input: DeviceInput.Alarm): AlarmCentral
    suspend fun deleteAlarmCentral(id: String)

    suspend fun getVideoDevices(): List<VideoDevice>
    suspend fun createVideoDevice(input: DeviceInput.Video): VideoDevice
    suspend fun updateVideoDevice(id: String, input: DeviceInput.Video): VideoDevice
    suspend fun deleteVideoDevice(id: String)
}
