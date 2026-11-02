package com.isaquefontinele.devicemanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.data.WriteOutcome
import com.isaquefontinele.devicemanager.core.insights.DeviceInfo
import com.isaquefontinele.devicemanager.core.insights.summarize
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: DeviceRepository) : ViewModel() {

    private val controls = MutableStateFlow(Controls())
    private val refresh = MutableStateFlow(RefreshState(isRefreshing = true))

    private val messageChannel = Channel<DashboardMessage>(Channel.BUFFERED)
    val messages: Flow<DashboardMessage> = messageChannel.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> =
        combine(repository.observeDevices(), controls, refresh) { devices, controls, refresh ->
            DashboardUiState(
                query = controls.query,
                filter = controls.filter,
                totalDevicesCount = devices.size,
                summary = summarize(devices.map { DeviceInfo(it is VideoDevice, it.isFavorite) }),
                content = content(devices, controls, refresh),
                isRefreshing = refresh.isRefreshing,
                selectedDevice = controls.actionsFor,
                removalDeviceCandidate = controls.removalDeviceCandidate,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DashboardUiState())

    init {
        refresh()
        viewModelScope.launch {
            repository.observeRejectedChanges().collect { changes ->
                changes.forEach { change ->
                    messageChannel.send(DashboardMessage.ChangeRejected(change.deviceName))
                    repository.acknowledgeRejectedChange(change.id)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refresh.value = RefreshState(isRefreshing = true)
            val error = repository.refresh().exceptionOrNull()?.toErrorReason()
            refresh.value = RefreshState(isRefreshing = false, error = error)
            // With saved devices on screen the failure is only a snackbar; without them it is the error state.
            if (error != null && repository.observeDevices().first().isNotEmpty()) {
                messageChannel.send(DashboardMessage.RefreshFailed(error))
            }
        }
    }

    fun onQueryChange(query: String) = controls.update { it.copy(query = query) }

    fun onFilterChange(filter: DeviceFilter) = controls.update { it.copy(filter = filter) }

    fun onShowActions(device: Device) = controls.update { it.copy(actionsFor = device) }

    fun onDismissActions() = controls.update { it.copy(actionsFor = null) }

    fun onToggleFavorite(device: Device) {
        controls.update { it.copy(actionsFor = null) }
        viewModelScope.launch {
            val isFavorite = !device.isFavorite
            repository.setFavorite(device.id, isFavorite)
            messageChannel.send(DashboardMessage.FavoriteChanged(isFavorite))
        }
    }

    fun onRemoveRequest(device: Device) = controls.update {
        it.copy(actionsFor = null, removalDeviceCandidate = device)
    }

    fun onRemoveDismiss() = controls.update { it.copy(removalDeviceCandidate = null) }

    fun onRemoveConfirm() {
        val device = controls.value.removalDeviceCandidate ?: return
        controls.update { it.copy(removalDeviceCandidate = null) }
        viewModelScope.launch {
            val message = repository.delete(device).fold(
                onSuccess = { outcome ->
                    when (outcome) {
                        WriteOutcome.SENT -> DashboardMessage.DeviceRemoved
                        WriteOutcome.QUEUED -> DashboardMessage.DeviceRemovedOffline
                    }
                },
                onFailure = { DashboardMessage.RemoveFailed(it.toErrorReason()) },
            )
            messageChannel.send(message)
        }
    }

    private fun content(devices: List<Device>, controls: Controls, refresh: RefreshState): DashboardContent {
        if (devices.isEmpty()) {
            return when {
                refresh.isRefreshing -> DashboardContent.Loading
                refresh.error != null -> DashboardContent.Error(refresh.error)
                else -> DashboardContent.Empty
            }
        }
        val visible = devices.filter { it.matches(controls.filter) && it.matches(controls.query) }
        return if (visible.isEmpty()) DashboardContent.NoResults else DashboardContent.Devices(visible)
    }

    private data class Controls(
        val query: String = "",
        val filter: DeviceFilter = DeviceFilter.ALL,
        val actionsFor: Device? = null,
        val removalDeviceCandidate: Device? = null,
    )

    private data class RefreshState(val isRefreshing: Boolean = false, val error: ErrorReason? = null)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
