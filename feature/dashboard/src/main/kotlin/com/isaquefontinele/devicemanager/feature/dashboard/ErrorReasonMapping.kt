package com.isaquefontinele.devicemanager.feature.dashboard

import com.isaquefontinele.devicemanager.core.data.DataError

internal fun Throwable.toErrorReason(): ErrorReason = when (this) {
    is DataError.NoConnection, is DataError.Timeout -> ErrorReason.NO_CONNECTION
    is DataError.Unauthorized -> ErrorReason.UNAUTHORIZED
    else -> ErrorReason.UNEXPECTED
}
