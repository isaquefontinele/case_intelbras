package com.isaquefontinele.devicemanager.core.network

import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import retrofit2.Response

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_CONFLICT = 409
private const val HTTP_SERVER_ERROR = 500

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Runs a Retrofit call translating every failure into a [NetworkError]. Cancellation is rethrown.
 * [HttpException] is not kept as cause: its code and server message are carried by the [NetworkError].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: NetworkError) {
    throw e
} catch (e: HttpException) {
    throw httpError(e.code(), e.response()?.errorBody()?.string())
} catch (e: SocketTimeoutException) {
    throw NetworkError.Timeout(e)
} catch (e: UnknownHostException) {
    throw NetworkError.NoConnection(e)
} catch (e: ConnectException) {
    throw NetworkError.NoConnection(e)
} catch (e: InterruptedIOException) {
    throw NetworkError.Timeout(e)
} catch (e: IOException) {
    throw NetworkError.NoConnection(e)
} catch (e: SerializationException) {
    throw NetworkError.Unknown(e)
} catch (e: Exception) {
    throw NetworkError.Unknown(e)
}

/** For endpoints returning [Response] directly (no body), where Retrofit does not throw on error codes. */
internal fun Response<*>.requireSuccess() {
    if (!isSuccessful) throw httpError(code(), errorBody()?.string())
}

internal fun httpError(code: Int, errorBody: String?): NetworkError {
    val message = parseErrorMessage(errorBody)
    return when {
        code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN -> NetworkError.Unauthorized()
        code == HTTP_NOT_FOUND -> NetworkError.NotFound()
        code == HTTP_CONFLICT -> NetworkError.Conflict(message)
        code >= HTTP_SERVER_ERROR -> NetworkError.Server(code)
        else -> NetworkError.BadRequest(code, message)
    }
}

/** NestJS error body: `{"statusCode": 409, "message": "..." | ["...", ...], "error": "Conflict"}`. */
internal fun parseErrorMessage(errorBody: String?): String? {
    if (errorBody.isNullOrBlank()) return null
    return runCatching {
        when (val message = errorJson.parseToJsonElement(errorBody).let { it as? JsonObject }?.get("message")) {
            is JsonPrimitive -> message.content
            is JsonArray -> message.joinToString("\n") { it.jsonPrimitive.content }
            else -> null
        }
    }.getOrNull()
}
