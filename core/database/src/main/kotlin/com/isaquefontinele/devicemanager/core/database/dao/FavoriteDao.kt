package com.isaquefontinele.devicemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FavoriteDao {

    /** Resolves the id in the statement itself, so a sync that replaces a local id cannot get in between. */
    @Query(
        """
        INSERT OR IGNORE INTO favorites (deviceId)
        VALUES (COALESCE((SELECT serverId FROM device_id_aliases WHERE localId = :deviceId), :deviceId))
        """,
    )
    suspend fun insert(deviceId: String)

    @Query(
        """
        DELETE FROM favorites
        WHERE deviceId = COALESCE((SELECT serverId FROM device_id_aliases WHERE localId = :deviceId), :deviceId)
        """,
    )
    suspend fun delete(deviceId: String)

    suspend fun setFavorite(deviceId: String, isFavorite: Boolean) {
        if (isFavorite) insert(deviceId) else delete(deviceId)
    }
}
