package com.isaquefontinele.devicemanager.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.isaquefontinele.devicemanager.core.database.dao.DeviceDao
import com.isaquefontinele.devicemanager.core.database.dao.FavoriteDao
import com.isaquefontinele.devicemanager.core.database.entity.DeviceEntity
import com.isaquefontinele.devicemanager.core.model.DeviceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceDaoTest {

    private lateinit var database: DeviceDatabase
    private lateinit var deviceDao: DeviceDao
    private lateinit var favoriteDao: FavoriteDao

    private val alarm = DeviceEntity(
        id = "a1",
        type = DeviceType.ALARM,
        name = "House",
        password = "1234",
        macAddress = "1A2B3C4D5E6F",
    )
    private val camera = DeviceEntity(
        id = "v1",
        type = DeviceType.VIDEO,
        name = "garage",
        password = "secret",
        serial = "ABC123",
        username = "admin",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DeviceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        deviceDao = database.deviceDao()
        favoriteDao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `devices are sorted by name ignoring case and flagged as favorite`() = runTest {
        deviceDao.upsert(listOf(alarm, camera, alarm.copy(id = "a2", name = "Office")))
        favoriteDao.setFavorite("v1", isFavorite = true)

        val devices = deviceDao.observeDevices().first()

        assertEquals(listOf("v1", "a1", "a2"), devices.map { it.device.id })
        assertEquals(listOf(true, false, false), devices.map { it.isFavorite })
    }

    @Test
    fun `upserting an existing device updates it and keeps the favorite`() = runTest {
        deviceDao.upsert(listOf(alarm))
        favoriteDao.setFavorite("a1", isFavorite = true)

        deviceDao.upsert(listOf(alarm.copy(name = "Renamed")))

        val device = deviceDao.observeDevice("a1").first()!!
        assertEquals("Renamed", device.device.name)
        assertEquals(true, device.isFavorite)
    }

    @Test
    fun `replaceAll removes missing devices and keeps favorites of the remaining ones`() = runTest {
        deviceDao.upsert(listOf(alarm, camera))
        favoriteDao.setFavorite("a1", isFavorite = true)
        favoriteDao.setFavorite("v1", isFavorite = true)

        deviceDao.replaceAll(listOf(alarm.copy(name = "House 2")))

        val devices = deviceDao.observeDevices().first()
        assertEquals(listOf("a1"), devices.map { it.device.id })
        assertEquals("House 2", devices.single().device.name)
        assertEquals(true, devices.single().isFavorite)

        // The cascade removed v1's favorite, so it does not come back if the device reappears.
        deviceDao.upsert(listOf(camera))
        assertEquals(false, deviceDao.observeDevice("v1").first()!!.isFavorite)
    }

    @Test
    fun `replaceAll with an empty list clears the table`() = runTest {
        deviceDao.upsert(listOf(alarm, camera))

        deviceDao.replaceAll(emptyList())

        assertEquals(emptyList<Any>(), deviceDao.observeDevices().first())
    }

    @Test
    fun `setFavorite toggles and is idempotent`() = runTest {
        deviceDao.upsert(listOf(alarm))

        favoriteDao.setFavorite("a1", isFavorite = true)
        favoriteDao.setFavorite("a1", isFavorite = true)
        assertEquals(true, deviceDao.observeDevice("a1").first()!!.isFavorite)

        favoriteDao.setFavorite("a1", isFavorite = false)
        assertEquals(false, deviceDao.observeDevice("a1").first()!!.isFavorite)
    }

    @Test
    fun `observeDevice emits changes and null after deletion`() = runTest {
        deviceDao.upsert(listOf(alarm))

        deviceDao.observeDevice("a1").test {
            assertEquals(false, awaitItem()!!.isFavorite)

            favoriteDao.setFavorite("a1", isFavorite = true)
            assertEquals(true, awaitItem()!!.isFavorite)

            deviceDao.deleteById("a1")
            assertNull(awaitItem())
        }
    }
}
