package com.isaquefontinele.devicemanager.core.model.validation

import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.MacAddress

enum class DeviceField {
    NAME,
    MAC_ADDRESS,
    SERIAL,
    USERNAME,
    PASSWORD,
}

enum class FieldError {
    REQUIRED,
    INVALID_MAC_ADDRESS,
    INVALID_ALARM_PASSWORD,
    ALREADY_IN_USE,
}

data class ValidationResult(val errors: Map<DeviceField, FieldError>) {
    val isValid: Boolean get() = errors.isEmpty()
}

object DeviceInputValidator {
    private val alarmPasswordPattern = Regex("^(\\d{4}|\\d{6})$")

    fun validate(input: DeviceInput): ValidationResult {
        val errors = buildMap {
            if (input.name.isBlank()) put(DeviceField.NAME, FieldError.REQUIRED)
            when (input) {
                is DeviceInput.Alarm -> validateAlarm(input)
                is DeviceInput.Video -> validateVideo(input)
            }
        }
        return ValidationResult(errors)
    }

    private fun MutableMap<DeviceField, FieldError>.validateAlarm(input: DeviceInput.Alarm) {
        when {
            input.macAddress.isBlank() -> put(DeviceField.MAC_ADDRESS, FieldError.REQUIRED)
            !MacAddress.isValid(input.macAddress) -> put(DeviceField.MAC_ADDRESS, FieldError.INVALID_MAC_ADDRESS)
        }
        when {
            input.password.isEmpty() -> put(DeviceField.PASSWORD, FieldError.REQUIRED)
            !alarmPasswordPattern.matches(input.password) ->
                put(DeviceField.PASSWORD, FieldError.INVALID_ALARM_PASSWORD)
        }
    }

    private fun MutableMap<DeviceField, FieldError>.validateVideo(input: DeviceInput.Video) {
        if (input.serial.isBlank()) put(DeviceField.SERIAL, FieldError.REQUIRED)
        if (input.username.isBlank()) put(DeviceField.USERNAME, FieldError.REQUIRED)
        if (input.password.isEmpty()) put(DeviceField.PASSWORD, FieldError.REQUIRED)
    }
}

fun DeviceInput.normalized(): DeviceInput = when (this) {
    is DeviceInput.Alarm -> copy(name = name.trim(), macAddress = MacAddress.normalize(macAddress))
    is DeviceInput.Video -> copy(name = name.trim(), serial = serial.trim(), username = username.trim())
}
