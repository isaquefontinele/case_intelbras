package com.isaquefontinele.devicemanager.feature.deviceform

import androidx.annotation.StringRes
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError

@StringRes
internal fun FieldError.messageRes(field: DeviceField): Int = when (this) {
    FieldError.REQUIRED -> R.string.deviceform_error_required
    FieldError.INVALID_MAC_ADDRESS -> R.string.deviceform_error_mac_address
    FieldError.INVALID_ALARM_PASSWORD -> R.string.deviceform_error_alarm_password
    FieldError.INVALID_SERIAL_LENGTH -> R.string.deviceform_error_serial_length
    FieldError.TOO_LONG -> R.string.deviceform_error_too_long
    FieldError.ALREADY_IN_USE -> when (field) {
        DeviceField.MAC_ADDRESS -> R.string.deviceform_error_mac_in_use
        DeviceField.SERIAL -> R.string.deviceform_error_serial_in_use
        else -> R.string.deviceform_error_in_use
    }
}

@StringRes
internal fun FormError.messageRes(): Int = when (this) {
    FormError.INVALID_FIELDS -> R.string.deviceform_save_failed_invalid_fields
    FormError.NO_CONNECTION -> R.string.deviceform_save_failed_no_connection
    FormError.UNAUTHORIZED -> R.string.deviceform_save_failed_unauthorized
    FormError.NOT_FOUND, FormError.UNEXPECTED -> R.string.deviceform_save_failed
}
