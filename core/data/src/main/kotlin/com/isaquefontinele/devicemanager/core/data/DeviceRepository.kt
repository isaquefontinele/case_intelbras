package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import kotlinx.coroutines.flow.Flow

/**
 * The local database is the single source of truth: reads always come from it, and writes go to the API first
 * and are then saved locally. When the server cannot be reached, a write is saved locally as pending instead,
 * reported as [WriteOutcome.QUEUED], and sent later by [syncPending] or [refresh]. Failures are returned as
 * [DataError].
 */
interface DeviceRepository {

    /** All devices sorted by name, with their local favorite flag. */
    fun observeDevices(): Flow<List<Device>>

    /** Emits `null` when the device does not exist (or was removed). */
    fun observeDevice(id: String): Flow<Device?>

    /**
     * Tries to send pending changes, then downloads both device lists and replaces the local copy. On failure
     * the cached data is kept.
     */
    suspend fun refresh(): Result<Unit>

    /**
     * Sends pending changes in order and downloads the lists. Fails, keeping what was not sent, when the server
     * cannot take them yet. Changes the server refuses (409, other 4xx) cannot succeed on a retry and are
     * discarded: the device goes back to what the server has.
     */
    suspend fun syncPending(): Result<Unit>

    /** Validates and normalizes [input] before sending it; returns [DataError.Invalid] on field errors. */
    suspend fun create(input: DeviceInput): Result<WriteOutcome>

    suspend fun update(id: String, input: DeviceInput): Result<WriteOutcome>

    suspend fun delete(device: Device): Result<WriteOutcome>

    /** Favorites are local only: they are never sent to the API and survive every [refresh]. */
    suspend fun setFavorite(id: String, isFavorite: Boolean)
}
