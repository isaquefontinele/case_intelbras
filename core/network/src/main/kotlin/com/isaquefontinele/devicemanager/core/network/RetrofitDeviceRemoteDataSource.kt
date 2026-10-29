package com.isaquefontinele.devicemanager.core.network

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.network.dto.toDomain
import com.isaquefontinele.devicemanager.core.network.dto.toRequest
import javax.inject.Inject

internal class RetrofitDeviceRemoteDataSource @Inject constructor(private val api: DeviceApi) :
    DeviceRemoteDataSource {

    override suspend fun getAlarmCentrals(): List<AlarmCentral> = apiCall {
        api.getAlarmCentrals().data.map { it.toDomain() }
    }

    override suspend fun createAlarmCentral(input: DeviceInput.Alarm): AlarmCentral = apiCall {
        api.createAlarmCentral(input.toRequest()).toDomain()
    }

    override suspend fun updateAlarmCentral(id: String, input: DeviceInput.Alarm): AlarmCentral = apiCall {
        api.updateAlarmCentral(id, input.toRequest()).toDomain()
    }

    override suspend fun deleteAlarmCentral(id: String) = apiCall {
        api.deleteAlarmCentral(id).requireSuccess()
    }

    override suspend fun getVideoDevices(): List<VideoDevice> = apiCall {
        api.getVideoDevices().data.map { it.toDomain() }
    }

    override suspend fun createVideoDevice(input: DeviceInput.Video): VideoDevice = apiCall {
        api.createVideoDevice(input.toRequest()).toDomain()
    }

    override suspend fun updateVideoDevice(id: String, input: DeviceInput.Video): VideoDevice = apiCall {
        api.updateVideoDevice(id, input.toRequest()).toDomain()
    }

    override suspend fun deleteVideoDevice(id: String) = apiCall {
        api.deleteVideoDevice(id).requireSuccess()
    }
}
