package com.isaquefontinele.devicemanager.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.database.DeviceDatabase
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError
import com.isaquefontinele.devicemanager.core.network.NetworkError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineFirstDeviceRepositoryTest {

    private lateinit var database: DeviceDatabase
    private lateinit var remote: FakeDeviceRemoteDataSource
    private lateinit var repository: OfflineFirstDeviceRepository

    private val alarm = AlarmCentral(id = "a1", name = "House", macAddress = "1A2B3C4D5E6F", password = "1234")
    private val camera = VideoDevice(id = "v1", name = "Garage", serial = "ABC", username = "admin", password = "pw")
    private val alarmInput = DeviceInput.Alarm(name = " Office ", macAddress = "aa:bb:cc:dd:ee:ff", password = "1234")
    private val videoInput = DeviceInput.Video(
        name = "Door",
        serial = "XYZ9876543210",
        username = "admin",
        password = "pw",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DeviceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remote = FakeDeviceRemoteDataSource()
        repository = OfflineFirstDeviceRepository(remote, database.deviceDao(), database.favoriteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun devices(): List<Device> = repository.observeDevices().first()

    private suspend fun seedServerAndRefresh() {
        remote.alarms[alarm.id] = alarm
        remote.videos[camera.id] = camera
        repository.refresh().getOrThrow()
    }

    @Test
    fun `refresh stores both device types sorted by name`() = runTest {
        seedServerAndRefresh()

        assertEquals(listOf(camera, alarm), devices())
    }

    @Test
    fun `favorites survive a refresh that changes the device`() = runTest {
        seedServerAndRefresh()
        repository.setFavorite(alarm.id, isFavorite = true)
        remote.alarms[alarm.id] = alarm.copy(name = "House renamed")

        repository.refresh().getOrThrow()

        assertEquals(alarm.copy(name = "House renamed", isFavorite = true), repository.observeDevice(alarm.id).first())
    }

    @Test
    fun `refresh removes devices deleted on the server`() = runTest {
        seedServerAndRefresh()
        remote.videos.clear()

        repository.refresh().getOrThrow()

        assertEquals(listOf(alarm), devices())
    }

    @Test
    fun `failed refresh keeps the cached devices`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()

        val result = repository.refresh()

        assertTrue(result.exceptionOrNull() is DataError.NoConnection)
        assertEquals(listOf(camera, alarm), devices())
    }

    @Test
    fun `unauthorized is reported as such`() = runTest {
        remote.failure = NetworkError.Unauthorized()

        assertTrue(repository.refresh().exceptionOrNull() is DataError.Unauthorized)
    }

    @Test
    fun `create with invalid input returns field errors without calling the api`() = runTest {
        val result = repository.create(DeviceInput.Alarm(name = "", macAddress = "123", password = "12"))

        val error = result.exceptionOrNull() as DataError.Invalid
        assertEquals(
            mapOf(
                DeviceField.NAME to FieldError.REQUIRED,
                DeviceField.MAC_ADDRESS to FieldError.INVALID_MAC_ADDRESS,
                DeviceField.PASSWORD to FieldError.INVALID_ALARM_PASSWORD,
            ),
            error.errors,
        )
        assertEquals(0, remote.calls)
    }

    @Test
    fun `create sends normalized input and saves the device locally`() = runTest {
        repository.create(alarmInput).getOrThrow()

        val created = remote.alarms.values.single()
        assertEquals("Office", created.name)
        assertEquals("AABBCCDDEEFF", created.macAddress)
        assertEquals(listOf<Device>(created), devices())
    }

    @Test
    fun `duplicate mac address or serial becomes a field error`() = runTest {
        repository.create(alarmInput).getOrThrow()
        repository.create(videoInput).getOrThrow()

        val alarmError = repository.create(alarmInput).exceptionOrNull() as DataError.Invalid
        val videoError = repository.create(videoInput).exceptionOrNull() as DataError.Invalid

        assertEquals(mapOf(DeviceField.MAC_ADDRESS to FieldError.ALREADY_IN_USE), alarmError.errors)
        assertEquals(mapOf(DeviceField.SERIAL to FieldError.ALREADY_IN_USE), videoError.errors)
    }

    @Test
    fun `update saves the new values and keeps the favorite`() = runTest {
        seedServerAndRefresh()
        repository.setFavorite(camera.id, isFavorite = true)

        repository.update(camera.id, videoInput).getOrThrow()

        assertEquals(
            VideoDevice(camera.id, "Door", "XYZ9876543210", "admin", "pw", isFavorite = true),
            repository.observeDevice(camera.id).first(),
        )
    }

    @Test
    fun `update of a device removed on the server drops the local copy`() = runTest {
        seedServerAndRefresh()
        remote.videos.clear()

        val result = repository.update(camera.id, videoInput)

        assertTrue(result.exceptionOrNull() is DataError.NotFound)
        assertEquals(null, repository.observeDevice(camera.id).first())
    }

    @Test
    fun `delete removes the device on the server and locally`() = runTest {
        seedServerAndRefresh()
        repository.setFavorite(alarm.id, isFavorite = true)

        repository.delete(alarm).getOrThrow()

        assertTrue(alarm.id !in remote.alarms)
        assertEquals(listOf<Device>(camera), devices())
    }

    @Test
    fun `delete of a device already removed on the server succeeds`() = runTest {
        seedServerAndRefresh()
        remote.alarms.clear()

        assertTrue(repository.delete(alarm).isSuccess)
        assertEquals(listOf<Device>(camera), devices())
    }

    @Test
    fun `failed delete keeps the local device`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.Timeout()

        val result = repository.delete(alarm)

        assertTrue(result.exceptionOrNull() is DataError.Timeout)
        assertEquals(listOf(camera, alarm), devices())
    }

    @Test
    fun `setFavorite is local only`() = runTest {
        seedServerAndRefresh()
        val callsBefore = remote.calls

        repository.setFavorite(camera.id, isFavorite = true)

        assertEquals(callsBefore, remote.calls)
        assertEquals(listOf(true, false), devices().map { it.isFavorite })
    }
}
