package com.isaquefontinele.devicemanager.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.database.entity.FavoriteEntity

@Database(
    entities = [DeviceEntity::class, FavoriteEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class DeviceDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun favoriteDao(): FavoriteDao
}
