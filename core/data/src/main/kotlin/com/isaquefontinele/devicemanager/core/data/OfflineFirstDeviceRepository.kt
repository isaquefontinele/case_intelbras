package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.database.entity.SyncState
import com.isaquefontinele.devicemanager.core.database.entity.toDomain
import com.isaquefontinele.devicemanager.core.database.entity.toEntity
import com.isaquefontinele.devicemanager.core.database.entity.toInput
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.validation.DeviceInputValidator
import com.isaquefontinele.devicemanager.core.model.validation.normalized
import com.isaquefontinele.devicemanager.core.network.DeviceRemoteDataSource
import com.isaquefontinele.devicemanager.core.network.NetworkError
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class OfflineFirstDeviceRepository @Inject constructor(
    private val remote: DeviceRemoteDataSource,
    private val deviceDao: DeviceDao,
    private val favoriteDao: FavoriteDao,
) : DeviceRepository {

    /**
     * One write or sync at a time, including its network calls. Otherwise a sync could overwrite an edit made
     * while it waits for the server, or a list downloaded before a create could remove the new device.
     */
    private val writeLock = Mutex()

    override fun observeDevices(): Flow<List<Device>> =
        deviceDao.observeDevices().map { devices -> devices.map { it.toDomain() } }

    override fun observeDevice(id: String): Flow<Device?> = deviceDao.observeDevice(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = runCatchingData {
        writeLock.withLock {
            // A failure here does not stop the download: what was not sent stays pending.
            runCatchingData { sendPending() }
            downloadDevices()
        }
    }

    override suspend fun syncPending(): Result<Unit> = runCatchingData {
        writeLock.withLock {
            sendPending()
            downloadDevices()
        }
    }

    override suspend fun create(input: DeviceInput): Result<Unit> = withValidInput(input) { valid ->
        sendOrQueue(
            send = { deviceDao.upsert(listOf(sendCreate(valid).toEntity())) },
            queue = { deviceDao.upsert(listOf(valid.toEntity(newLocalId(), SyncState.PENDING_CREATE))) },
        )
    }

    override suspend fun update(id: String, input: DeviceInput): Result<Unit> = withValidInput(input) { valid ->
        when (val state = deviceDao.getById(id)?.syncState) {
            SyncState.PENDING_DELETE -> throw DataError.NotFound()
            // The server gets the latest values when the pending change is sent.
            SyncState.PENDING_CREATE, SyncState.PENDING_UPDATE -> deviceDao.upsert(listOf(valid.toEntity(id, state)))
            SyncState.SYNCED, null -> sendOrQueue(
                send = { sendUpdateOrDropLocal(id, valid) },
                queue = { deviceDao.upsert(listOf(valid.toEntity(id, SyncState.PENDING_UPDATE))) },
            )
        }
    }

    override suspend fun delete(device: Device): Result<Unit> = runCatchingData {
        writeLock.withLock {
            val entity = device.toEntity()
            if (deviceDao.getById(device.id)?.syncState == SyncState.PENDING_CREATE) {
                // The server never knew about it.
                deviceDao.deleteById(device.id)
            } else {
                sendOrQueue(
                    send = { sendDeleteAndDropLocal(entity) },
                    queue = { deviceDao.upsert(listOf(entity.copy(syncState = SyncState.PENDING_DELETE))) },
                )
            }
        }
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean) {
        favoriteDao.setFavorite(id, isFavorite)
    }

    private suspend fun withValidInput(input: DeviceInput, block: suspend (DeviceInput) -> Unit): Result<Unit> {
        val normalized = input.normalized()
        val validation = DeviceInputValidator.validate(normalized)
        if (!validation.isValid) return Result.failure(DataError.Invalid(validation.errors))
        return runCatchingData(normalized) { writeLock.withLock { block(normalized) } }
    }

    /**
     * A timeout also queues: the server may have applied the write, but sending it again is safe. An update
     * repeats the same values, a delete answers 404, and a create answers 409 and the download brings the device.
     */
    private suspend fun sendOrQueue(send: suspend () -> Unit, queue: suspend () -> Unit) {
        try {
            send()
        } catch (_: NetworkError.NoConnection) {
            queue()
        } catch (_: NetworkError.Timeout) {
            queue()
        }
    }

    private suspend fun downloadDevices() {
        val devices = coroutineScope {
            val alarms = async { remote.getAlarmCentrals() }
            val videos = async { remote.getVideoDevices() }
            alarms.await() + videos.await()
        }
        deviceDao.replaceAll(devices.map { it.toEntity() })
    }

    /** Stops at the first failure that may go away, so nothing is lost and the order is kept. */
    private suspend fun sendPending() {
        deviceDao.getPending().forEach { pending ->
            try {
                sendChange(pending)
            } catch (_: NetworkError.Conflict) {
                discardChange(pending)
            } catch (_: NetworkError.BadRequest) {
                discardChange(pending)
            }
        }
    }

    private suspend fun sendChange(pending: DeviceEntity) {
        when (pending.syncState) {
            SyncState.PENDING_CREATE ->
                deviceDao.replaceLocalId(pending.id, sendCreate(pending.toInput()).toEntity())
            SyncState.PENDING_UPDATE -> try {
                sendUpdateOrDropLocal(pending.id, pending.toInput())
            } catch (_: NetworkError.NotFound) {
                // Removed elsewhere, and sendUpdateOrDropLocal already dropped the local copy.
            }
            SyncState.PENDING_DELETE -> sendDeleteAndDropLocal(pending)
            SyncState.SYNCED -> Unit
        }
    }

    private suspend fun discardChange(pending: DeviceEntity) {
        if (pending.syncState == SyncState.PENDING_CREATE) {
            deviceDao.deleteById(pending.id)
        } else {
            // The download that follows brings back the server's values.
            deviceDao.upsert(listOf(pending.copy(syncState = SyncState.SYNCED)))
        }
    }

    private suspend fun sendUpdateOrDropLocal(id: String, input: DeviceInput) {
        val device = try {
            sendUpdate(id, input)
        } catch (e: NetworkError.NotFound) {
            // Removed elsewhere: drop the stale local copy and still report the failure.
            deviceDao.deleteById(id)
            throw e
        }
        deviceDao.upsert(listOf(device.toEntity()))
    }

    private suspend fun sendDeleteAndDropLocal(entity: DeviceEntity) {
        try {
            when (entity.type) {
                DeviceType.ALARM -> remote.deleteAlarmCentral(entity.id)
                DeviceType.VIDEO -> remote.deleteVideoDevice(entity.id)
            }
        } catch (_: NetworkError.NotFound) {
            // Already gone on the server, which is the outcome the user asked for.
        }
        deviceDao.deleteById(entity.id)
    }

    private suspend fun sendCreate(input: DeviceInput): Device = when (input) {
        is DeviceInput.Alarm -> remote.createAlarmCentral(input)
        is DeviceInput.Video -> remote.createVideoDevice(input)
    }

    private suspend fun sendUpdate(id: String, input: DeviceInput): Device = when (input) {
        is DeviceInput.Alarm -> remote.updateAlarmCentral(id, input)
        is DeviceInput.Video -> remote.updateVideoDevice(id, input)
    }

    private fun newLocalId() = "local-${UUID.randomUUID()}"
}
