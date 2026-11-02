package com.isaquefontinele.devicemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.isaquefontinele.devicemanager.core.database.entity.RejectedChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RejectedChangeDao {

    @Insert
    suspend fun insert(change: RejectedChangeEntity)

    @Query("SELECT * FROM rejected_changes ORDER BY id")
    fun observeAll(): Flow<List<RejectedChangeEntity>>

    @Query("DELETE FROM rejected_changes WHERE id = :id")
    suspend fun delete(id: Long)
}
