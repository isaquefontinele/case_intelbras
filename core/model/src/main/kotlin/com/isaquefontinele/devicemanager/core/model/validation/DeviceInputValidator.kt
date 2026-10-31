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
    INVALID_SERIAL_LENGTH,
    TOO_LONG,
    ALREADY_IN_USE,
}

data class ValidationResult(val errors: Map<DeviceField, FieldError>) {
    val isValid: Boolean get() = errors.isEmpty()
}

object DeviceInputValidator {
    // Limits enforced by the API, which rejects other values with a 400.
    const val SERIAL_LENGTH = 13
    const val MAX_TEXT_LENGTH = 200

    private val alarmPasswordPattern = Regex("^(\\d{4}|\\d{6})$")

    fun validate(input: DeviceInput): ValidationResult {
        val errors = buildMap {
            validateText(DeviceField.NAME, input.name, input.name.isBlank())
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
        when {
            input.serial.isBlank() -> put(DeviceField.SERIAL, FieldError.REQUIRED)
            input.serial.length != SERIAL_LENGTH -> put(DeviceField.SERIAL, FieldError.INVALID_SERIAL_LENGTH)
        }
        validateText(DeviceField.USERNAME, input.username, input.username.isBlank())
        validateText(DeviceField.PASSWORD, input.password, input.password.isEmpty())
    }

    private fun MutableMap<DeviceField, FieldError>.validateText(field: DeviceField, value: String, missing: Boolean) {
        when {
            missing -> put(field, FieldError.REQUIRED)
            value.length > MAX_TEXT_LENGTH -> put(field, FieldError.TOO_LONG)
        }
    }
}

fun DeviceInput.normalized(): DeviceInput = when (this) {
    is DeviceInput.Alarm -> copy(name = name.trim(), macAddress = MacAddress.normalize(macAddress))
    is DeviceInput.Video -> copy(name = name.trim(), serial = serial.trim(), username = username.trim())
}
