package com.isaquefontinele.devicemanager.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.dao.RejectedChangeDao
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.database.entity.DeviceIdAliasEntity
import com.isaquefontinele.devicemanager.core.database.entity.FavoriteEntity
import com.isaquefontinele.devicemanager.core.database.entity.RejectedChangeEntity

@Database(
    entities = [
        DeviceEntity::class,
        FavoriteEntity::class,
        DeviceIdAliasEntity::class,
        RejectedChangeEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
abstract class DeviceDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun rejectedChangeDao(): RejectedChangeDao
}
