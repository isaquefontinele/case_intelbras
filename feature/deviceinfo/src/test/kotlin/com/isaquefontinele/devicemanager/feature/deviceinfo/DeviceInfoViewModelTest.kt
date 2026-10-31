package com.isaquefontinele.devicemanager.feature.deviceinfo

import androidx.lifecycle.SavedStateHandle
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.testing.FakeDeviceRepository
import com.isaquefontinele.devicemanager.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DeviceInfoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alarm = AlarmCentral(id = "a1", name = "Casa", macAddress = "A0B1C2D3E4F5", password = "1234")
    private val repository = FakeDeviceRepository(listOf(alarm))

    private fun viewModel(id: String) = DeviceInfoViewModel(SavedStateHandle(mapOf("deviceId" to id)), repository)

    @Test
    fun `shows the saved device and follows its changes`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel("a1")
        viewModel.uiState.launchIn(backgroundScope)
        assertEquals(DeviceInfoUiState.Content(alarm), viewModel.uiState.value)

        repository.setFavorite("a1", isFavorite = true)

        assertEquals(DeviceInfoUiState.Content(alarm.copy(isFavorite = true)), viewModel.uiState.value)
    }

    @Test
    fun `unknown device is not found`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel("missing")
        viewModel.uiState.launchIn(backgroundScope)

        assertEquals(DeviceInfoUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `device removed while open becomes not found`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel("a1")
        viewModel.uiState.launchIn(backgroundScope)

        repository.delete(alarm)

        assertEquals(DeviceInfoUiState.NotFound, viewModel.uiState.value)
    }
}
