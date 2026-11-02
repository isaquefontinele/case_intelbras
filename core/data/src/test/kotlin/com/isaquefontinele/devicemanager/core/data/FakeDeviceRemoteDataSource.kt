package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.network.DeviceRemoteDataSource
import com.isaquefontinele.devicemanager.core.network.NetworkError

/** In-memory server. Set [failure] to make every call throw it. */
class FakeDeviceRemoteDataSource : DeviceRemoteDataSource {

    val alarms = linkedMapOf<String, AlarmCentral>()
    val videos = linkedMapOf<String, VideoDevice>()
    var failure: NetworkError? = null
    var failUpdates: NetworkError? = null
    var failDeletes: NetworkError? = null
    var calls = 0
        private set
    private var nextId = 0

    override suspend fun getAlarmCentrals(): List<AlarmCentral> = call { alarms.values.toList() }

    override suspend fun createAlarmCentral(input: DeviceInput.Alarm): AlarmCentral = call {
        if (alarms.values.any { it.macAddress == input.macAddress }) throw NetworkError.Conflict("duplicate")
        AlarmCentral("alarm-${nextId++}", input.name, input.macAddress, input.password).also { alarms[it.id] = it }
    }

    override suspend fun updateAlarmCentral(id: String, input: DeviceInput.Alarm): AlarmCentral = call {
        failUpdates?.let { throw it }
        if (id !in alarms) throw NetworkError.NotFound()
        AlarmCentral(id, input.name, input.macAddress, input.password).also { alarms[id] = it }
    }

    override suspend fun deleteAlarmCentral(id: String) = call {
        failDeletes?.let { throw it }
        alarms.remove(id) ?: throw NetworkError.NotFound()
        Unit
    }

    override suspend fun getVideoDevices(): List<VideoDevice> = call { videos.values.toList() }

    override suspend fun createVideoDevice(input: DeviceInput.Video): VideoDevice = call {
        if (videos.values.any { it.serial == input.serial }) throw NetworkError.Conflict("duplicate")
        VideoDevice("video-${nextId++}", input.name, input.serial, input.username, input.password)
            .also { videos[it.id] = it }
    }

    override suspend fun updateVideoDevice(id: String, input: DeviceInput.Video): VideoDevice = call {
        failUpdates?.let { throw it }
        if (id !in videos) throw NetworkError.NotFound()
        VideoDevice(id, input.name, input.serial, input.username, input.password).also { videos[id] = it }
    }

    override suspend fun deleteVideoDevice(id: String) = call {
        failDeletes?.let { throw it }
        videos.remove(id) ?: throw NetworkError.NotFound()
        Unit
    }

    private inline fun <T> call(block: () -> T): T {
        calls++
        failure?.let { throw it }
        return block()
    }
}
