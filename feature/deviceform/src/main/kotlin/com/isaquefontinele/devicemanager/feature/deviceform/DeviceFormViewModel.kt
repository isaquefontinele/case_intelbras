package com.isaquefontinele.devicemanager.feature.deviceform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaquefontinele.devicemanager.core.data.DataError
import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.data.WriteOutcome
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.MacAddress
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.feature.deviceform.navigation.DeviceFormDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DeviceRepository,
) : ViewModel() {

    private val deviceId: String? = savedStateHandle[DeviceFormDestination::deviceId.name]

    private val _uiState = MutableStateFlow(DeviceFormUiState(isNew = deviceId == null, isLoading = deviceId != null))
    val uiState: StateFlow<DeviceFormUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<DeviceFormEvent>(Channel.BUFFERED)
    val events: Flow<DeviceFormEvent> = eventChannel.receiveAsFlow()

    init {
        if (deviceId != null) loadDevice(deviceId)
    }

    private fun loadDevice(id: String) {
        viewModelScope.launch {
            when (val device = repository.observeDevice(id).first()) {
                null -> eventChannel.send(DeviceFormEvent.DeviceNotFound)
                is AlarmCentral -> _uiState.update {
                    it.copy(
                        type = DeviceType.ALARM,
                        name = device.name,
                        macAddress = MacAddress.format(device.macAddress),
                        password = device.password,
                        isLoading = false,
                    )
                }

                is VideoDevice -> _uiState.update {
                    it.copy(
                        type = DeviceType.VIDEO,
                        name = device.name,
                        serial = device.serial,
                        username = device.username,
                        password = device.password,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onTypeChange(type: DeviceType) {
        if (!_uiState.value.isNew) return
        _uiState.update { it.copy(type = type, errors = emptyMap()) }
    }

    fun onNameChange(value: String) = edit(DeviceField.NAME) { copy(name = value) }

    fun onMacAddressChange(value: String) = edit(DeviceField.MAC_ADDRESS) { copy(macAddress = value) }

    fun onSerialChange(value: String) = edit(DeviceField.SERIAL) { copy(serial = value) }

    fun onUsernameChange(value: String) = edit(DeviceField.USERNAME) { copy(username = value) }

    fun onPasswordChange(value: String) = edit(DeviceField.PASSWORD) { copy(password = value) }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val input = state.toInput()
            val result = if (deviceId == null) repository.create(input) else repository.update(deviceId, input)
            _uiState.update { it.copy(isSaving = false) }
            result.fold(
                onSuccess = { outcome ->
                    eventChannel.send(
                        DeviceFormEvent.Saved(isNew = deviceId == null, isQueued = outcome == WriteOutcome.QUEUED),
                    )
                },
                onFailure = { handleFailure(it) },
            )
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        when (error) {
            is DataError.Invalid -> _uiState.update { it.copy(errors = error.errors) }
            is DataError.NotFound -> eventChannel.send(DeviceFormEvent.DeviceNotFound)
            is DataError.NoConnection, is DataError.Timeout ->
                eventChannel.send(DeviceFormEvent.SaveFailed(FormError.NO_CONNECTION))

            is DataError.Unauthorized -> eventChannel.send(DeviceFormEvent.SaveFailed(FormError.UNAUTHORIZED))
            else -> eventChannel.send(DeviceFormEvent.SaveFailed(FormError.UNEXPECTED))
        }
    }

    private fun edit(field: DeviceField, change: DeviceFormUiState.() -> DeviceFormUiState) {
        _uiState.update { it.change().copy(errors = it.errors - field) }
    }

    private fun DeviceFormUiState.toInput(): DeviceInput = when (type) {
        DeviceType.ALARM -> DeviceInput.Alarm(name = name, macAddress = macAddress, password = password)
        DeviceType.VIDEO -> DeviceInput.Video(name = name, serial = serial, username = username, password = password)
    }
}
