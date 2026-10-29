package com.isaquefontinele.devicemanager.core.network

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiCallTest {

    @Test
    fun `socket timeout maps to Timeout`() = runTest {
        val error = runCatching { apiCall { throw SocketTimeoutException() } }.exceptionOrNull()

        assertTrue(error is NetworkError.Timeout)
    }

    @Test
    fun `unknown host maps to NoConnection`() = runTest {
        val error = runCatching { apiCall { throw UnknownHostException() } }.exceptionOrNull()

        assertTrue(error is NetworkError.NoConnection)
    }

    @Test
    fun `cancellation is rethrown untouched`() = runTest {
        val cancellation = CancellationException("cancelled")

        val error = runCatching { apiCall { throw cancellation } }.exceptionOrNull()

        assertTrue(error === cancellation)
    }

    @Test
    fun `403 is also treated as Unauthorized`() {
        assertTrue(httpError(403, null) is NetworkError.Unauthorized)
    }

    @Test
    fun `other 4xx map to BadRequest with the server message`() {
        val error = httpError(400, """{"statusCode":400,"message":["name should not be empty"]}""")

        assertTrue(error is NetworkError.BadRequest)
        assertEquals("name should not be empty", error.message)
    }

    @Test
    fun `parseErrorMessage joins array messages`() {
        assertEquals("a\nb", parseErrorMessage("""{"message":["a","b"]}"""))
    }

    @Test
    fun `parseErrorMessage ignores non json bodies`() {
        assertNull(parseErrorMessage("<html>Bad Gateway</html>"))
        assertNull(parseErrorMessage(null))
    }
}
