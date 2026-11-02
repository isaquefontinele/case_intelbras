package com.isaquefontinele.devicemanager.core.testing

import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.data.WriteOutcome
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [DeviceRepository] with configurable results. */
class FakeDeviceRepository(devices: List<Device> = emptyList()) : DeviceRepository {

    val devices = MutableStateFlow(devices)

    /** Result of the next refreshes. */
    var refreshResult: Result<Unit> = Result.success(Unit)

    /** When set, refresh suspends until it completes. */
    var refreshGate: CompletableDeferred<Unit>? = null
    var refreshCalls = 0
        private set

    var createResult: Result<WriteOutcome> = Result.success(WriteOutcome.SENT)
    var updateResult: Result<WriteOutcome> = Result.success(WriteOutcome.SENT)
    var deleteResult: Result<WriteOutcome> = Result.success(WriteOutcome.SENT)

    /** When set, create and update suspend until it completes. */
    var saveGate: CompletableDeferred<Unit>? = null

    /** Inputs received by [create] and [update], in order. */
    val savedInputs = mutableListOf<DeviceInput>()

    override fun observeDevices(): Flow<List<Device>> = devices

    override fun observeDevice(id: String): Flow<Device?> = devices.map { list -> list.find { it.id == id } }

    override suspend fun refresh(): Result<Unit> {
        refreshCalls++
        refreshGate?.await()
        return refreshResult
    }

    override suspend fun syncPending(): Result<Unit> = Result.success(Unit)

    override suspend fun create(input: DeviceInput): Result<WriteOutcome> {
        savedInputs += input
        saveGate?.await()
        return createResult
    }

    override suspend fun update(id: String, input: DeviceInput): Result<WriteOutcome> {
        savedInputs += input
        saveGate?.await()
        return updateResult
    }

    override suspend fun delete(device: Device): Result<WriteOutcome> = deleteResult.onSuccess {
        devices.update { list -> list.filterNot { it.id == device.id } }
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean) {
        devices.update { list ->
            list.map {
                if (it.id != id) {
                    it
                } else {
                    when (it) {
                        is AlarmCentral -> it.copy(isFavorite = isFavorite)
                        is VideoDevice -> it.copy(isFavorite = isFavorite)
                    }
                }
            }
        }
    }
}
