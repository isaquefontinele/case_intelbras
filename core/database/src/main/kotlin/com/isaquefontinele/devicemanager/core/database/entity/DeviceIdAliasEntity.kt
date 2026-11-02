package com.isaquefontinele.devicemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The server id given to a device created offline. A screen or sheet opened before the sync still holds the
 * local id, and this is how it keeps finding the device.
 */
@Entity(tableName = "device_id_aliases")
data class DeviceIdAliasEntity(@PrimaryKey val localId: String, val serverId: String)
