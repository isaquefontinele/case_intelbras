package com.isaquefontinele.devicemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isaquefontinele.devicemanager.core.model.DeviceType

/**
 * Both device types share one table (API ids are UUIDs, so they never collide).
 * Type-specific columns are null for the other type.
 */
@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val type: DeviceType,
    val name: String,
    val password: String,
    val macAddress: String? = null,
    val serial: String? = null,
    val username: String? = null,
)
