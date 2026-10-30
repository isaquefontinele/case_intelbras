package com.isaquefontinele.devicemanager.core.data

import com.isaquefontinele.devicemanager.core.network.NetworkError
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMappingTest {

    @Test
    fun `server and bad request errors become unexpected with the server message`() {
        val badRequest = NetworkError.BadRequest(code = 400, message = "name must be a string").toDataError()

        assertTrue(badRequest is DataError.Unexpected)
        assertEquals("name must be a string", badRequest.message)
        assertTrue(NetworkError.Server(code = 500).toDataError() is DataError.Unexpected)
    }

    @Test
    fun `conflict without an input is unexpected`() {
        assertTrue(NetworkError.Conflict("duplicate").toDataError() is DataError.Unexpected)
    }

    @Test
    fun `non network failures become unexpected`() {
        assertTrue(IOException("disk").toDataError() is DataError.Unexpected)
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is not wrapped`() = runTest {
        runCatchingData { throw CancellationException("cancelled") }
    }
}
