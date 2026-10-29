package com.isaquefontinele.devicemanager.core.network

sealed class NetworkError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable? = null) : NetworkError(cause = cause)

    class Timeout(cause: Throwable? = null) : NetworkError(cause = cause)

    /** 401/403 — missing or invalid token. The API answers 401 even though the docs say 403. */
    class Unauthorized : NetworkError()

    class NotFound : NetworkError()

    /** 409 — MAC address (alarm) or serial number (video) already used by another device. */
    class Conflict(message: String?) : NetworkError(message)

    /** Others 4xx, carrying the server message when available. */
    class BadRequest(val code: Int, message: String?) : NetworkError(message)

    class Server(val code: Int) : NetworkError()

    /** Unexpected payload or any other failure. */
    class Unknown(cause: Throwable? = null) : NetworkError(cause = cause)
}
