package com.isaquefontinele.devicemanager.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.database.DeviceDatabase
import com.isaquefontinele.devicemanager.core.database.entity.SyncState
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

    private suspend fun pendingIds(): List<String> = database.deviceDao().getPending().map { it.id }

    private suspend fun syncState(id: String): SyncState? = database.deviceDao().getById(id)?.syncState

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
        remote.failure = NetworkError.Server(500)

        val result = repository.delete(alarm)

        assertTrue(result.exceptionOrNull() is DataError.Unexpected)
        assertEquals(listOf(camera, alarm), devices())
        assertEquals(emptyList<String>(), pendingIds())
    }

    @Test
    fun `setFavorite is local only`() = runTest {
        seedServerAndRefresh()
        val callsBefore = remote.calls

        repository.setFavorite(camera.id, isFavorite = true)

        assertEquals(callsBefore, remote.calls)
        assertEquals(listOf(true, false), devices().map { it.isFavorite })
    }

    @Test
    fun `create without connection is saved as pending and shown`() = runTest {
        remote.failure = NetworkError.NoConnection()

        assertTrue(repository.create(videoInput).isSuccess)

        val device = devices().single() as VideoDevice
        assertEquals("Door", device.name)
        assertEquals(SyncState.PENDING_CREATE, syncState(device.id))
    }

    @Test
    fun `invalid input is not saved as pending`() = runTest {
        remote.failure = NetworkError.NoConnection()

        val result = repository.create(videoInput.copy(serial = "short"))

        assertTrue(result.exceptionOrNull() is DataError.Invalid)
        assertEquals(emptyList<Device>(), devices())
    }

    @Test
    fun `server errors are reported and not saved as pending`() = runTest {
        remote.failure = NetworkError.Server(500)

        assertTrue(repository.create(videoInput).isFailure)

        assertEquals(emptyList<Device>(), devices())
    }

    @Test
    fun `sync sends a device created offline and keeps its favorite under the server id`() = runTest {
        remote.failure = NetworkError.NoConnection()
        repository.create(videoInput).getOrThrow()
        val localId = devices().single().id
        repository.setFavorite(localId, isFavorite = true)
        remote.failure = null

        repository.syncPending().getOrThrow()

        val created = remote.videos.values.single()
        assertEquals(listOf<Device>(created.copy(isFavorite = true)), devices())
        assertEquals(null, syncState(localId))
        assertEquals(SyncState.SYNCED, syncState(created.id))
    }

    @Test
    fun `update without connection is shown and sent by the next refresh`() = runTest {
        seedServerAndRefresh()
        repository.setFavorite(camera.id, isFavorite = true)
        remote.failure = NetworkError.Timeout()

        assertTrue(repository.update(camera.id, videoInput).isSuccess)

        assertEquals(SyncState.PENDING_UPDATE, syncState(camera.id))
        assertEquals("Door", repository.observeDevice(camera.id).first()?.name)
        assertEquals("Garage", remote.videos.getValue(camera.id).name)

        remote.failure = null
        repository.refresh().getOrThrow()

        assertEquals("Door", remote.videos.getValue(camera.id).name)
        assertEquals(SyncState.SYNCED, syncState(camera.id))
        assertEquals(true, repository.observeDevice(camera.id).first()?.isFavorite)
    }

    @Test
    fun `delete without connection hides the device until the server confirms`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()

        assertTrue(repository.delete(alarm).isSuccess)

        assertEquals(listOf<Device>(camera), devices())
        assertEquals(SyncState.PENDING_DELETE, syncState(alarm.id))

        remote.failure = null
        repository.syncPending().getOrThrow()

        assertTrue(alarm.id !in remote.alarms)
        assertEquals(null, syncState(alarm.id))
    }

    @Test
    fun `refresh does not bring back a device with a pending delete when sending fails`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()
        repository.delete(alarm).getOrThrow()
        remote.failure = null
        remote.failDeletes = NetworkError.Server(500)

        repository.refresh().getOrThrow()

        assertEquals(listOf<Device>(camera), devices())
        assertEquals(listOf(alarm.id), pendingIds())
    }

    @Test
    fun `editing a device created offline stays one create with the latest values`() = runTest {
        remote.failure = NetworkError.NoConnection()
        repository.create(videoInput).getOrThrow()
        val localId = devices().single().id

        repository.update(localId, videoInput.copy(name = "Front door")).getOrThrow()
        assertEquals(SyncState.PENDING_CREATE, syncState(localId))

        remote.failure = null
        repository.syncPending().getOrThrow()

        assertEquals("Front door", remote.videos.values.single().name)
    }

    @Test
    fun `editing a pending device does not call the api`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()
        repository.update(camera.id, videoInput).getOrThrow()
        val callsBefore = remote.calls

        repository.update(camera.id, videoInput.copy(name = "Front door")).getOrThrow()

        assertEquals(callsBefore, remote.calls)
        assertEquals("Front door", repository.observeDevice(camera.id).first()?.name)
    }

    @Test
    fun `deleting a device created offline never reaches the server`() = runTest {
        remote.failure = NetworkError.NoConnection()
        repository.create(videoInput).getOrThrow()
        val device = devices().single()
        remote.failure = null
        val callsBefore = remote.calls

        repository.delete(device).getOrThrow()

        assertEquals(callsBefore, remote.calls)
        assertEquals(emptyList<Device>(), devices())
        assertEquals(emptyList<String>(), pendingIds())
    }

    @Test
    fun `deleting a device with a pending update sends only the delete`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()
        repository.update(camera.id, videoInput).getOrThrow()
        repository.delete(camera).getOrThrow()
        remote.failure = null

        repository.syncPending().getOrThrow()

        assertTrue(camera.id !in remote.videos)
        assertEquals(listOf<Device>(alarm), devices())
    }

    @Test
    fun `failed sync keeps every pending change`() = runTest {
        remote.failure = NetworkError.NoConnection()
        repository.create(videoInput).getOrThrow()
        repository.create(alarmInput).getOrThrow()

        val result = repository.syncPending()

        assertTrue(result.exceptionOrNull() is DataError.NoConnection)
        assertEquals(2, pendingIds().size)
    }

    @Test
    fun `refused create is discarded`() = runTest {
        remote.failure = NetworkError.NoConnection()
        repository.create(videoInput).getOrThrow()
        remote.failure = null
        val taken = VideoDevice("v9", "Other", videoInput.serial, "admin", "pw")
        remote.videos[taken.id] = taken

        repository.syncPending().getOrThrow()

        assertEquals(listOf<Device>(taken), devices())
        assertEquals(emptyList<String>(), pendingIds())
    }

    @Test
    fun `refused update goes back to the server values`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()
        repository.update(camera.id, videoInput).getOrThrow()
        remote.failure = null
        remote.failUpdates = NetworkError.Conflict("duplicate")

        repository.syncPending().getOrThrow()

        assertEquals(camera, repository.observeDevice(camera.id).first())
        assertEquals(emptyList<String>(), pendingIds())
    }

    @Test
    fun `pending update of a device removed on the server is dropped`() = runTest {
        seedServerAndRefresh()
        remote.failure = NetworkError.NoConnection()
        repository.update(camera.id, videoInput).getOrThrow()
        remote.failure = null
        remote.videos.clear()

        repository.syncPending().getOrThrow()

        assertEquals(null, syncState(camera.id))
    }
}
