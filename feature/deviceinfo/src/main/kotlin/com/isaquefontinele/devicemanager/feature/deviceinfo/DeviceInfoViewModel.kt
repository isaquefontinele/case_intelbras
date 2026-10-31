package com.isaquefontinele.devicemanager.feature.deviceinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.feature.deviceinfo.navigation.DeviceInfoDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface DeviceInfoUiState {
    data object Loading : DeviceInfoUiState
    
    data object NotFound : DeviceInfoUiState

    data class Content(val device: Device) : DeviceInfoUiState
}

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: DeviceRepository) :
    ViewModel() {

    private val deviceId: String = checkNotNull(savedStateHandle[DeviceInfoDestination::deviceId.name])

    val uiState: StateFlow<DeviceInfoUiState> = repository.observeDevice(deviceId)
        .map { device -> device?.let(DeviceInfoUiState::Content) ?: DeviceInfoUiState.NotFound }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DeviceInfoUiState.Loading)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
