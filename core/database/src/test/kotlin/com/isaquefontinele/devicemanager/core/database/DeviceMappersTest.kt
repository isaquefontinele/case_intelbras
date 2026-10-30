package com.isaquefontinele.devicemanager.core.database

import com.isaquefontinele.devicemanager.core.database.entity.DeviceWithFavorite
import com.isaquefontinele.devicemanager.core.database.entity.toDomain
import com.isaquefontinele.devicemanager.core.database.entity.toEntity
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceMappersTest {

    @Test
    fun `alarm central survives the entity round trip`() {
        val alarm = AlarmCentral(id = "a1", name = "House", macAddress = "1A2B3C4D5E6F", password = "1234")

        assertEquals(alarm, DeviceWithFavorite(alarm.toEntity(), isFavorite = false).toDomain())
    }

    @Test
    fun `video device survives the entity round trip`() {
        val video = VideoDevice(id = "v1", name = "Garage", serial = "ABC", username = "admin", password = "pw")

        assertEquals(video, DeviceWithFavorite(video.toEntity(), isFavorite = false).toDomain())
    }

    @Test
    fun `favorite flag comes from the join, not from the entity`() {
        val video = VideoDevice(id = "v1", name = "Garage", serial = "ABC", username = "admin", password = "pw")

        val domain = DeviceWithFavorite(video.toEntity(), isFavorite = true).toDomain()

        assertEquals(true, domain.isFavorite)
    }
}
