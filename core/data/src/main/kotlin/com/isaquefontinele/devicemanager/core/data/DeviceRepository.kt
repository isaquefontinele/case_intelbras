package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import kotlinx.coroutines.flow.Flow

/**
 * The local database is the single source of truth: reads always come from it, and writes go to the API first
 * and are then saved locally. Failures are returned as [DataError].
 */
interface DeviceRepository {

    /** All devices sorted by name, with their local favorite flag. */
    fun observeDevices(): Flow<List<Device>>

    /** Emits `null` when the device does not exist (or was removed). */
    fun observeDevice(id: String): Flow<Device?>

    /** Downloads both device lists and replaces the local copy. On failure the cached data is kept. */
    suspend fun refresh(): Result<Unit>

    /** Validates and normalizes [input] before sending it; returns [DataError.Invalid] on field errors. */
    suspend fun create(input: DeviceInput): Result<Unit>

    suspend fun update(id: String, input: DeviceInput): Result<Unit>

    suspend fun delete(device: Device): Result<Unit>

    /** Favorites are local only: they are never sent to the API and survive every [refresh]. */
    suspend fun setFavorite(id: String, isFavorite: Boolean)
}
