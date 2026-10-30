package com.isaquefontinele.devicemanager.core.database.entity

import androidx.room.Embedded

data class DeviceWithFavorite(@Embedded val device: DeviceEntity, val isFavorite: Boolean)
