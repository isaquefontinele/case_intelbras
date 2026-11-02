package com.isaquefontinele.devicemanager.core.database.di

import android.content.Context
import androidx.room.Room
import com.isaquefontinele.devicemanager.core.database.DeviceDatabase
import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.dao.RejectedChangeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DeviceDatabase =
        Room.databaseBuilder(context, DeviceDatabase::class.java, "devices.db").build()

    @Provides
    fun provideDeviceDao(database: DeviceDatabase): DeviceDao = database.deviceDao()

    @Provides
    fun provideFavoriteDao(database: DeviceDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideRejectedChangeDao(database: DeviceDatabase): RejectedChangeDao = database.rejectedChangeDao()
}
