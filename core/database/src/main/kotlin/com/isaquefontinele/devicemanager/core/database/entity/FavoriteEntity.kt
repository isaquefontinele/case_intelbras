package com.isaquefontinele.devicemanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Favorites live in their own table so a sync, which only writes to [DeviceEntity], never touches them.
 * Deleting a device cascades to its favorite.
 */
@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FavoriteEntity(@PrimaryKey val deviceId: String)
