package com.isaquefontinele.devicemanager.feature.dashboard

import app.cash.turbine.test
import com.isaquefontinele.devicemanager.core.data.DataError
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.testing.FakeDeviceRepository
import com.isaquefontinele.devicemanager.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alarm =
        AlarmCentral(id = "a1", name = "Central de alarme", macAddress = "A0B1C2D3E4F5", password = "1234")
    private val camera1 = VideoDevice(
        id = "v1",
        name = "Câmera 1",
        serial = "ABCDE123",
        username = "admin",
        password = "pw",
        isFavorite = true,
    )
    private val camera2 =
        VideoDevice(id = "v2", name = "Câmera 2", serial = "XYZ999", username = "admin", password = "pw")
    private val all = listOf(alarm, camera1, camera2)

    /** Keeps [DashboardViewModel.uiState] collected, as the screen would. */
    private fun TestScope.subscribe(viewModel: DashboardViewModel) {
        viewModel.uiState.launchIn(backgroundScope)
    }

    private fun TestScope.viewModel(repository: FakeDeviceRepository) =
        DashboardViewModel(repository).also { subscribe(it) }

    private fun DashboardViewModel.visible() = (uiState.value.content as DashboardContent.Devices).devices

    @Test
    fun `shows loading while the first refresh runs with nothing saved`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository().apply { refreshGate = CompletableDeferred() }

        val viewModel = viewModel(repository)
        assertEquals(DashboardContent.Loading, viewModel.uiState.value.content)

        repository.refreshGate!!.complete(Unit)
        assertEquals(DashboardContent.Empty, viewModel.uiState.value.content)
    }

    @Test
    fun `saved devices are shown while refreshing`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository(all).apply { refreshGate = CompletableDeferred() }

        val viewModel = viewModel(repository)

        assertEquals(true, viewModel.uiState.value.isRefreshing)
        assertEquals(all, viewModel.visible())
        assertEquals(3, viewModel.uiState.value.totalDevicesCount)
    }

    @Test
    fun `failed first load without saved devices shows the error state`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository().apply { refreshResult = Result.failure(DataError.NoConnection()) }

        val viewModel = viewModel(repository)

        assertEquals(DashboardContent.Error(ErrorReason.NO_CONNECTION), viewModel.uiState.value.content)
    }

    @Test
    fun `failed refresh with saved devices keeps them and shows a message`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository(all).apply { refreshResult = Result.failure(DataError.Unauthorized()) }
        val viewModel = DashboardViewModel(repository)

        viewModel.messages.test {
            assertEquals(DashboardMessage.RefreshFailed(ErrorReason.UNAUTHORIZED), awaitItem())
        }
        subscribe(viewModel)
        assertEquals(all, viewModel.visible())
    }

    @Test
    fun `retry after an error loads the devices`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository().apply { refreshResult = Result.failure(DataError.Timeout()) }
        val viewModel = viewModel(repository)

        repository.refreshResult = Result.success(Unit)
        repository.devices.value = all
        viewModel.refresh()

        assertEquals(all, viewModel.visible())
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `filters by type and favorites`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel(FakeDeviceRepository(all))

        viewModel.onFilterChange(DeviceFilter.CAMERAS)
        assertEquals(listOf(camera1, camera2), viewModel.visible())

        viewModel.onFilterChange(DeviceFilter.ALARMS)
        assertEquals(listOf(alarm), viewModel.visible())

        viewModel.onFilterChange(DeviceFilter.FAVORITES)
        assertEquals(listOf(camera1), viewModel.visible())
        assertEquals(3, viewModel.uiState.value.totalDevicesCount)
    }

    @Test
    fun `searches by name, serial and mac address with or without separators`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel(FakeDeviceRepository(all))

        viewModel.onQueryChange("câmera")
        assertEquals(listOf(camera1, camera2), viewModel.visible())

        viewModel.onQueryChange("xyz")
        assertEquals(listOf(camera2), viewModel.visible())

        viewModel.onQueryChange("a0:b1:c2")
        assertEquals(listOf(alarm), viewModel.visible())

        viewModel.onQueryChange("  ")
        assertEquals(all, viewModel.visible())
    }

    @Test
    fun `search combined with a filter that excludes everything shows no results`() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = viewModel(FakeDeviceRepository(all))

            viewModel.onFilterChange(DeviceFilter.ALARMS)
            viewModel.onQueryChange("Câmera")

            assertEquals(DashboardContent.NoResults, viewModel.uiState.value.content)
        }

    @Test
    fun `toggling favorite closes the sheet and updates the list`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository(all)
        val viewModel = viewModel(repository)
        viewModel.onShowActions(alarm)
        assertEquals(alarm, viewModel.uiState.value.selectedDevice)

        viewModel.messages.test {
            viewModel.onToggleFavorite(alarm)
            assertEquals(DashboardMessage.FavoriteChanged(isFavorite = true), awaitItem())
        }

        assertNull(viewModel.uiState.value.selectedDevice)
        viewModel.onFilterChange(DeviceFilter.FAVORITES)
        assertEquals(listOf(alarm.copy(isFavorite = true), camera1), viewModel.visible())
    }

    @Test
    fun `removal asks for confirmation and removes on confirm`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel(FakeDeviceRepository(all))
        viewModel.onShowActions(camera2)

        viewModel.onRemoveRequest(camera2)
        assertNull(viewModel.uiState.value.selectedDevice)
        assertEquals(camera2, viewModel.uiState.value.removalDeviceCandidate)

        viewModel.messages.test {
            viewModel.onRemoveConfirm()
            assertEquals(DashboardMessage.DeviceRemoved, awaitItem())
        }
        assertNull(viewModel.uiState.value.removalDeviceCandidate)
        assertEquals(listOf(alarm, camera1), viewModel.visible())
    }

    @Test
    fun `dismissing the confirmation keeps the device`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel(FakeDeviceRepository(all))

        viewModel.onRemoveRequest(camera2)
        viewModel.onRemoveDismiss()

        assertNull(viewModel.uiState.value.removalDeviceCandidate)
        assertEquals(all, viewModel.visible())
    }

    @Test
    fun `failed removal reports the reason`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceRepository(all).apply { deleteResult = Result.failure(DataError.NoConnection()) }
        val viewModel = viewModel(repository)
        viewModel.onRemoveRequest(alarm)

        viewModel.messages.test {
            viewModel.onRemoveConfirm()
            assertEquals(DashboardMessage.RemoveFailed(ErrorReason.NO_CONNECTION), awaitItem())
        }
        assertEquals(all, viewModel.visible())
    }
}
