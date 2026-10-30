package com.isaquefontinele.devicemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isaquefontinele.devicemanager.core.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    suspend fun setFavorite(deviceId: String, isFavorite: Boolean) {
        if (isFavorite) insert(FavoriteEntity(deviceId)) else delete(deviceId)
    }
}
