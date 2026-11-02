package com.isaquefontinele.devicemanager.feature.deviceform

import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError

data class DeviceFormUiState(
    /** `false` when editing: the type cannot change and the selector is hidden. */
    val isNew: Boolean = true,
    val type: DeviceType = DeviceType.VIDEO,
    val name: String = "",
    val macAddress: String = "",
    val serial: String = "",
    val username: String = "",
    val password: String = "",
    val errors: Map<DeviceField, FieldError> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
)

enum class FormError { NO_CONNECTION, UNAUTHORIZED, NOT_FOUND, UNEXPECTED }

sealed interface DeviceFormEvent {
    data class Saved(val isNew: Boolean, val isQueued: Boolean = false) : DeviceFormEvent
    data class SaveFailed(val error: FormError) : DeviceFormEvent
    data object DeviceNotFound : DeviceFormEvent
}
