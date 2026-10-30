package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError

sealed class DataError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable? = null) : DataError(cause = cause)

    class Timeout(cause: Throwable? = null) : DataError(cause = cause)

    class Unauthorized : DataError()

    class NotFound : DataError()

    /** Field errors found locally by the validator or reported by the server (409). */
    class Invalid(val errors: Map<DeviceField, FieldError>) : DataError()

    class Unexpected(message: String? = null, cause: Throwable? = null) : DataError(message, cause)
}
