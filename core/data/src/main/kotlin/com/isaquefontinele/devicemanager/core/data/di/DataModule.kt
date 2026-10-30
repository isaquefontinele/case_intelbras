package com.isaquefontinele.devicemanager.core.data.di

import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.data.OfflineFirstDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: OfflineFirstDeviceRepository): DeviceRepository
}
