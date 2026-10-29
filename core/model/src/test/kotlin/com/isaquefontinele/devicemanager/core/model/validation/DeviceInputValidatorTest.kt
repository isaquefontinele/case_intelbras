package com.isaquefontinele.devicemanager.core.model.validation

import com.isaquefontinele.devicemanager.core.model.DeviceInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceInputValidatorTest {

    private val validAlarm = DeviceInput.Alarm(name = "House", macAddress = "1A2B3C4D5E6F", password = "1234")
    private val validVideo = DeviceInput.Video(
        name = "Garage",
        serial = "ABCDEF1234567",
        username = "admin",
        password = "secret",
    )

    @Test
    fun `valid inputs have no errors`() {
        assertTrue(DeviceInputValidator.validate(validAlarm).isValid)
        assertTrue(DeviceInputValidator.validate(validVideo).isValid)
    }

    @Test
    fun `blank alarm fields are required`() {
        val result = DeviceInputValidator.validate(DeviceInput.Alarm(name = " ", macAddress = "", password = ""))

        assertEquals(
            mapOf(
                DeviceField.NAME to FieldError.REQUIRED,
                DeviceField.MAC_ADDRESS to FieldError.REQUIRED,
                DeviceField.PASSWORD to FieldError.REQUIRED,
            ),
            result.errors,
        )
    }

    @Test
    fun `blank video fields are required`() {
        val result = DeviceInputValidator.validate(
            DeviceInput.Video(name = "", serial = " ", username = "", password = ""),
        )

        assertEquals(
            mapOf(
                DeviceField.NAME to FieldError.REQUIRED,
                DeviceField.SERIAL to FieldError.REQUIRED,
                DeviceField.USERNAME to FieldError.REQUIRED,
                DeviceField.PASSWORD to FieldError.REQUIRED,
            ),
            result.errors,
        )
    }

    @Test
    fun `alarm mac address must be valid`() {
        val result = DeviceInputValidator.validate(validAlarm.copy(macAddress = "1A2B3C"))

        assertEquals(mapOf(DeviceField.MAC_ADDRESS to FieldError.INVALID_MAC_ADDRESS), result.errors)
    }

    @Test
    fun `alarm mac address accepts separators`() {
        assertTrue(DeviceInputValidator.validate(validAlarm.copy(macAddress = "1a:2b:3c:4d:5e:6f")).isValid)
    }

    @Test
    fun `alarm password accepts only 4 or 6 digits`() {
        listOf("1234", "123456").forEach { password ->
            assertTrue(password, DeviceInputValidator.validate(validAlarm.copy(password = password)).isValid)
        }
        listOf("123", "12345", "1234567", "12a4", "abcdef").forEach { password ->
            assertEquals(
                password,
                mapOf(DeviceField.PASSWORD to FieldError.INVALID_ALARM_PASSWORD),
                DeviceInputValidator.validate(validAlarm.copy(password = password)).errors,
            )
        }
    }

    @Test
    fun `video password has no format rule`() {
        assertTrue(DeviceInputValidator.validate(validVideo.copy(password = "a")).isValid)
    }

    @Test
    fun `normalized trims text fields and normalizes the mac address`() {
        val alarm = DeviceInput.Alarm(name = "  House ", macAddress = "1a:2b:3c:4d:5e:6f", password = " 1234")
        val video = DeviceInput.Video(name = " Garage ", serial = " ABC ", username = " admin ", password = " pw ")

        assertEquals(
            DeviceInput.Alarm(name = "House", macAddress = "1A2B3C4D5E6F", password = " 1234"),
            alarm.normalized(),
        )
        assertEquals(
            DeviceInput.Video(name = "Garage", serial = "ABC", username = "admin", password = " pw "),
            video.normalized(),
        )
    }
}
