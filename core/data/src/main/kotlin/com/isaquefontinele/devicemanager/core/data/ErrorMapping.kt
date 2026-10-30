package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError
import com.isaquefontinele.devicemanager.core.network.NetworkError
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs [block] and wraps any failure as a [DataError]. [input] tells which field a 409 refers to.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> runCatchingData(input: DeviceInput? = null, block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.toDataError(input))
}

internal fun Throwable.toDataError(input: DeviceInput? = null): DataError = when (this) {
    is DataError -> this
    is NetworkError.NoConnection -> DataError.NoConnection(this)
    is NetworkError.Timeout -> DataError.Timeout(this)
    is NetworkError.Unauthorized -> DataError.Unauthorized()
    is NetworkError.NotFound -> DataError.NotFound()
    is NetworkError.Conflict -> when (input) {
        is DeviceInput.Alarm -> DataError.Invalid(mapOf(DeviceField.MAC_ADDRESS to FieldError.ALREADY_IN_USE))
        is DeviceInput.Video -> DataError.Invalid(mapOf(DeviceField.SERIAL to FieldError.ALREADY_IN_USE))
        null -> DataError.Unexpected(message, this)
    }
    else -> DataError.Unexpected(message, this)
}
