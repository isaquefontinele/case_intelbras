package com.isaquefontinele.devicemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.database.entity.DeviceWithFavorite
import kotlinx.coroutines.flow.Flow

/** SQLite's bind-parameter limit on older Android versions. */
private const val SQLITE_MAX_VARIABLES = 999

@Dao
interface DeviceDao {

    @Query(
        """
        SELECT devices.*, favorites.deviceId IS NOT NULL AS isFavorite
        FROM devices LEFT JOIN favorites ON favorites.deviceId = devices.id
        WHERE devices.syncState != 'PENDING_DELETE'
        ORDER BY devices.name COLLATE NOCASE, devices.id
        """,
    )
    fun observeDevices(): Flow<List<DeviceWithFavorite>>

    @Query(
        """
        SELECT devices.*, favorites.deviceId IS NOT NULL AS isFavorite
        FROM devices LEFT JOIN favorites ON favorites.deviceId = devices.id
        WHERE devices.id = :id AND devices.syncState != 'PENDING_DELETE'
        """,
    )
    fun observeDevice(id: String): Flow<DeviceWithFavorite?>

    /** Includes rows pending deletion, which [observeDevices] hides. */
    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?

    /** Rows with a change the server has not received yet, oldest first. */
    @Query("SELECT * FROM devices WHERE syncState != 'SYNCED' ORDER BY rowid")
    suspend fun getPending(): List<DeviceEntity>

    /**
     * Upsert updates rows in place. `REPLACE` would delete and re-insert them, and the delete would cascade
     * to the favorites table.
     */
    @Upsert
    suspend fun upsert(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT id FROM devices WHERE syncState = 'SYNCED'")
    suspend fun getSyncedIds(): List<String>

    @Query("DELETE FROM devices WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE favorites SET deviceId = :newId WHERE deviceId = :oldId")
    suspend fun moveFavorite(oldId: String, newId: String)

    /**
     * Makes the synced rows match [devices] (the full server list): upserts every device and removes the
     * ones that are gone. Favorites of devices that remain are kept. Rows with a pending change are left as
     * they are, so the change is not lost before it reaches the server.
     */
    @Transaction
    suspend fun replaceAll(devices: List<DeviceEntity>) {
        val pendingIds = getPending().mapTo(HashSet()) { it.id }
        upsert(devices.filterNot { it.id in pendingIds })
        val remoteIds = devices.mapTo(HashSet()) { it.id }
        getSyncedIds()
            .filterNot { it in remoteIds }
            .chunked(SQLITE_MAX_VARIABLES)
            .forEach { deleteByIds(it) }
    }

    @Transaction
    suspend fun replaceLocalId(localId: String, device: DeviceEntity) {
        upsert(listOf(device))
        moveFavorite(localId, device.id)
        deleteById(localId)
    }
}
