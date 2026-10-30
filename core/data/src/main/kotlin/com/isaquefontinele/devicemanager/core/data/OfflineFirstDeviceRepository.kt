package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.entity.toDomain
import com.isaquefontinele.devicemanager.core.database.entity.toEntity
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.model.validation.DeviceInputValidator
import com.isaquefontinele.devicemanager.core.model.validation.normalized
import com.isaquefontinele.devicemanager.core.network.DeviceRemoteDataSource
import com.isaquefontinele.devicemanager.core.network.NetworkError
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineFirstDeviceRepository @Inject constructor(
    private val remote: DeviceRemoteDataSource,
    private val deviceDao: DeviceDao,
    private val favoriteDao: FavoriteDao,
) : DeviceRepository {

    override fun observeDevices(): Flow<List<Device>> =
        deviceDao.observeDevices().map { devices -> devices.map { it.toDomain() } }

    override fun observeDevice(id: String): Flow<Device?> = deviceDao.observeDevice(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = runCatchingData {
        val devices = coroutineScope {
            val alarms = async { remote.getAlarmCentrals() }
            val videos = async { remote.getVideoDevices() }
            alarms.await() + videos.await()
        }
        deviceDao.replaceAll(devices.map { it.toEntity() })
    }

    override suspend fun create(input: DeviceInput): Result<Unit> = withValidInput(input) { valid ->
        val device = when (valid) {
            is DeviceInput.Alarm -> remote.createAlarmCentral(valid)
            is DeviceInput.Video -> remote.createVideoDevice(valid)
        }
        deviceDao.upsert(listOf(device.toEntity()))
    }

    override suspend fun update(id: String, input: DeviceInput): Result<Unit> = withValidInput(input) { valid ->
        val device = try {
            when (valid) {
                is DeviceInput.Alarm -> remote.updateAlarmCentral(id, valid)
                is DeviceInput.Video -> remote.updateVideoDevice(id, valid)
            }
        } catch (e: NetworkError.NotFound) {
            // Removed elsewhere: drop the stale local copy and still report the failure.
            deviceDao.deleteById(id)
            throw e
        }
        deviceDao.upsert(listOf(device.toEntity()))
    }

    override suspend fun delete(device: Device): Result<Unit> = runCatchingData {
        try {
            when (device) {
                is AlarmCentral -> remote.deleteAlarmCentral(device.id)
                is VideoDevice -> remote.deleteVideoDevice(device.id)
            }
        } catch (_: NetworkError.NotFound) {
            // Already gone on the server, which is the outcome the user asked for.
        }
        deviceDao.deleteById(device.id)
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean) {
        favoriteDao.setFavorite(id, isFavorite)
    }

    private suspend fun withValidInput(input: DeviceInput, block: suspend (DeviceInput) -> Unit): Result<Unit> {
        val normalized = input.normalized()
        val validation = DeviceInputValidator.validate(normalized)
        if (!validation.isValid) return Result.failure(DataError.Invalid(validation.errors))
        return runCatchingData(normalized) { block(normalized) }
    }
}
