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
        ORDER BY devices.name COLLATE NOCASE, devices.id
        """,
    )
    fun observeDevices(): Flow<List<DeviceWithFavorite>>

    @Query(
        """
        SELECT devices.*, favorites.deviceId IS NOT NULL AS isFavorite
        FROM devices LEFT JOIN favorites ON favorites.deviceId = devices.id
        WHERE devices.id = :id
        """,
    )
    fun observeDevice(id: String): Flow<DeviceWithFavorite?>

    /**
     * Upsert updates rows in place. `REPLACE` would delete and re-insert them, and the delete would cascade
     * to the favorites table.
     */
    @Upsert
    suspend fun upsert(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT id FROM devices")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM devices WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    /**
     * Makes the table match [devices] (the full server list): upserts every device and removes the ones that
     * are gone. Favorites of devices that remain are kept.
     */
    @Transaction
    suspend fun replaceAll(devices: List<DeviceEntity>) {
        upsert(devices)
        val remoteIds = devices.mapTo(HashSet()) { it.id }
        getAllIds()
            .filterNot { it in remoteIds }
            .chunked(SQLITE_MAX_VARIABLES)
            .forEach { deleteByIds(it) }
    }
}
