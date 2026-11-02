package com.isaquefontinele.devicemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.database.entity.DeviceIdAliasEntity
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

    /** [id] can be the local id of a device that has since received its server id. */
    @Query(
        """
        SELECT devices.*, favorites.deviceId IS NOT NULL AS isFavorite
        FROM devices LEFT JOIN favorites ON favorites.deviceId = devices.id
        WHERE devices.id = COALESCE((SELECT serverId FROM device_id_aliases WHERE localId = :id), :id)
            AND devices.syncState != 'PENDING_DELETE'
        """,
    )
    fun observeDevice(id: String): Flow<DeviceWithFavorite?>

    /** Includes rows pending deletion, which [observeDevices] hides. */
    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?

    /** The id to use for [id] now: differs when it was the local id of a device already sent to the server. */
    @Query("SELECT COALESCE((SELECT serverId FROM device_id_aliases WHERE localId = :id), :id)")
    suspend fun currentId(id: String): String

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

    @Insert
    suspend fun insertAlias(alias: DeviceIdAliasEntity)

    @Transaction
    suspend fun replaceLocalId(localId: String, device: DeviceEntity) {
        upsert(listOf(device))
        moveFavorite(localId, device.id)
        deleteById(localId)
        insertAlias(DeviceIdAliasEntity(localId, device.id))
    }
}
