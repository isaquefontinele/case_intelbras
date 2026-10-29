package com.isaquefontinele.devicemanager.core.network

import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetrofitDeviceRemoteDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: RetrofitDeviceRemoteDataSource

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor("test-token")).build()
        val api = createDeviceApi(server.url("/").toString(), client, Json { ignoreUnknownKeys = true })
        dataSource = RetrofitDeviceRemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `getAlarmCentrals sends the bearer token and maps the list`() = runTest {
        server.enqueue(
            json(
                """{"count":1,"data":[{"id":"1","name":"House","macAddress":"1A2B3C4D5E6F","password":"1234"}]}""",
            ),
        )

        val result = dataSource.getAlarmCentrals()

        assertEquals(
            listOf(AlarmCentral(id = "1", name = "House", macAddress = "1A2B3C4D5E6F", password = "1234")),
            result,
        )
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/alarm-centrals", request.url.encodedPath)
        assertEquals("Bearer test-token", request.headers["Authorization"])
    }

    @Test
    fun `getVideoDevices ignores unknown fields`() = runTest {
        server.enqueue(
            json(
                """{"count":1,"data":[{"id":"2","name":"Garage","serial":"ABC","username":"admin",""" +
                    """"password":"pw","createdAt":"2026-01-01"}]}""",
            ),
        )

        val result = dataSource.getVideoDevices()

        assertEquals(
            listOf(VideoDevice(id = "2", name = "Garage", serial = "ABC", username = "admin", password = "pw")),
            result,
        )
    }

    @Test
    fun `createVideoDevice posts every field`() = runTest {
        server.enqueue(
            json("""{"id":"3","name":"Garage","serial":"ABC","username":"admin","password":"pw"}""", code = 201),
        )

        dataSource.createVideoDevice(
            DeviceInput.Video(name = "Garage", serial = "ABC", username = "admin", password = "pw"),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/video-devices", request.url.encodedPath)
        assertEquals(
            """{"name":"Garage","serial":"ABC","username":"admin","password":"pw"}""",
            request.body?.utf8(),
        )
    }

    @Test
    fun `updateAlarmCentral patches the device by id`() = runTest {
        server.enqueue(json("""{"id":"1","name":"Office","macAddress":"1A2B3C4D5E6F","password":"123456"}"""))

        val result = dataSource.updateAlarmCentral(
            id = "1",
            input = DeviceInput.Alarm(name = "Office", macAddress = "1A2B3C4D5E6F", password = "123456"),
        )

        assertEquals("Office", result.name)
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/alarm-centrals/1", request.url.encodedPath)
    }

    @Test
    fun `delete accepts an empty 200 response`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).build())

        dataSource.deleteVideoDevice("2")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/video-devices/2", request.url.encodedPath)
    }

    @Test
    fun `delete of a missing device fails with NotFound`() = runTest {
        server.enqueue(json("""{"statusCode":404,"message":"Not Found"}""", code = 404))

        val error = runCatching { dataSource.deleteAlarmCentral("9") }.exceptionOrNull()

        assertTrue(error is NetworkError.NotFound)
    }

    @Test
    fun `409 fails with Conflict carrying the server message`() = runTest {
        server.enqueue(
            json("""{"statusCode":409,"message":"MAC Address already exists","error":"Conflict"}""", code = 409),
        )

        val error = runCatching {
            dataSource.createAlarmCentral(DeviceInput.Alarm(name = "A", macAddress = "1A2B3C4D5E6F", password = "1234"))
        }.exceptionOrNull()

        assertTrue(error is NetworkError.Conflict)
        assertEquals("MAC Address already exists", error?.message)
    }

    @Test
    fun `401 fails with Unauthorized`() = runTest {
        server.enqueue(json("""{"statusCode":401,"message":"Unauthorized"}""", code = 401))

        val error = runCatching { dataSource.getVideoDevices() }.exceptionOrNull()

        assertTrue(error is NetworkError.Unauthorized)
    }

    @Test
    fun `500 fails with Server`() = runTest {
        server.enqueue(json("""{"statusCode":500,"message":"Internal server error"}""", code = 500))

        val error = runCatching { dataSource.getVideoDevices() }.exceptionOrNull()

        assertTrue(error is NetworkError.Server)
        assertEquals(500, (error as NetworkError.Server).code)
    }

    @Test
    fun `malformed payload fails with Unknown`() = runTest {
        server.enqueue(json("""{"unexpected":true}"""))

        val error = runCatching { dataSource.getAlarmCentrals() }.exceptionOrNull()

        assertTrue(error is NetworkError.Unknown)
    }

    @Test
    fun `unreachable server fails with NoConnection`() = runTest {
        server.close()

        val error = runCatching { dataSource.getAlarmCentrals() }.exceptionOrNull()

        assertTrue(error.toString(), error is NetworkError.NoConnection)
    }

    private fun json(body: String, code: Int = 200) = MockResponse.Builder()
        .code(code)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()
}
