package com.isaquefontinele.devicemanager.feature.deviceform

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.isaquefontinele.devicemanager.core.data.DataError
import com.isaquefontinele.devicemanager.core.data.WriteOutcome
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError
import com.isaquefontinele.devicemanager.core.testing.FakeDeviceRepository
import com.isaquefontinele.devicemanager.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DeviceFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alarm = AlarmCentral(id = "a1", name = "Casa", macAddress = "A0B1C2D3E4F5", password = "1234")
    private val camera = VideoDevice(id = "v1", name = "Garagem", serial = "ABC", username = "admin", password = "pw")
    private val repository = FakeDeviceRepository(listOf(alarm, camera))

    private fun newForm() = DeviceFormViewModel(SavedStateHandle(), repository)

    private fun editForm(id: String) = DeviceFormViewModel(SavedStateHandle(mapOf("deviceId" to id)), repository)

    @Test
    fun `new form starts empty with video selected`() {
        val state = newForm().uiState.value

        assertTrue(state.isNew)
        assertEquals(DeviceType.VIDEO, state.type)
        assertEquals("", state.name)
    }

    @Test
    fun `new video device is sent with the typed values`() = runTest {
        val viewModel = newForm()
        viewModel.onNameChange("Porta")
        viewModel.onSerialChange("XYZ")
        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("secret")

        viewModel.events.test {
            viewModel.onSave()
            assertEquals(DeviceFormEvent.Saved(isNew = true), awaitItem())
        }
        assertEquals(listOf(DeviceInput.Video("Porta", "XYZ", "admin", "secret")), repository.savedInputs)
    }

    @Test
    fun `a save without connection is reported as queued`() = runTest {
        repository.createResult = Result.success(WriteOutcome.QUEUED)
        val viewModel = newForm()

        viewModel.events.test {
            viewModel.onSave()
            assertEquals(DeviceFormEvent.Saved(isNew = true, isQueued = true), awaitItem())
        }
    }

    @Test
    fun `switching to alarm sends an alarm input and clears errors`() = runTest {
        val viewModel = newForm()
        repository.createResult = Result.failure(DataError.Invalid(mapOf(DeviceField.SERIAL to FieldError.REQUIRED)))
        viewModel.onSave()
        assertEquals(mapOf(DeviceField.SERIAL to FieldError.REQUIRED), viewModel.uiState.value.errors)

        viewModel.onTypeChange(DeviceType.ALARM)
        assertEquals(emptyMap<DeviceField, FieldError>(), viewModel.uiState.value.errors)

        repository.createResult = Result.success(WriteOutcome.SENT)
        viewModel.onNameChange("Casa")
        viewModel.onMacAddressChange("a0:b1:c2:d3:e4:f5")
        viewModel.onPasswordChange("1234")
        viewModel.onSave()
        assertEquals(DeviceInput.Alarm("Casa", "a0:b1:c2:d3:e4:f5", "1234"), repository.savedInputs.last())
    }

    @Test
    fun `field errors from the repository are shown and cleared when the field changes`() = runTest {
        val viewModel = newForm()
        repository.createResult = Result.failure(
            DataError.Invalid(
                mapOf(DeviceField.NAME to FieldError.REQUIRED, DeviceField.SERIAL to FieldError.ALREADY_IN_USE),
            ),
        )

        viewModel.onSave()
        viewModel.onNameChange("Porta")

        assertEquals(mapOf(DeviceField.SERIAL to FieldError.ALREADY_IN_USE), viewModel.uiState.value.errors)
    }

    @Test
    fun `edit form is filled with the saved device and formats the mac address`() {
        val state = editForm("a1").uiState.value

        assertFalse(state.isNew)
        assertFalse(state.isLoading)
        assertEquals(DeviceType.ALARM, state.type)
        assertEquals("Casa", state.name)
        assertEquals("A0:B1:C2:D3:E4:F5", state.macAddress)
        assertEquals("1234", state.password)
    }

    @Test
    fun `type cannot change while editing`() {
        val viewModel = editForm("v1")

        viewModel.onTypeChange(DeviceType.ALARM)

        assertEquals(DeviceType.VIDEO, viewModel.uiState.value.type)
    }

    @Test
    fun `saving an edit updates the device`() = runTest {
        val viewModel = editForm("v1")
        viewModel.onNameChange("Garagem 2")

        viewModel.events.test {
            viewModel.onSave()
            assertEquals(DeviceFormEvent.Saved(isNew = false), awaitItem())
        }
        assertEquals(listOf(DeviceInput.Video("Garagem 2", "ABC", "admin", "pw")), repository.savedInputs)
    }

    @Test
    fun `editing a device that no longer exists closes the form`() = runTest {
        val viewModel = editForm("missing")

        viewModel.events.test {
            assertEquals(DeviceFormEvent.DeviceNotFound, awaitItem())
        }
    }

    @Test
    fun `device removed on the server while editing closes the form`() = runTest {
        val viewModel = editForm("v1")
        repository.updateResult = Result.failure(DataError.NotFound())

        viewModel.events.test {
            viewModel.onSave()
            assertEquals(DeviceFormEvent.DeviceNotFound, awaitItem())
        }
    }

    @Test
    fun `connection and auth failures are reported`() = runTest {
        val viewModel = newForm()

        viewModel.events.test {
            repository.createResult = Result.failure(DataError.Timeout())
            viewModel.onSave()
            assertEquals(DeviceFormEvent.SaveFailed(FormError.NO_CONNECTION), awaitItem())

            repository.createResult = Result.failure(DataError.Unauthorized())
            viewModel.onSave()
            assertEquals(DeviceFormEvent.SaveFailed(FormError.UNAUTHORIZED), awaitItem())

            repository.createResult = Result.failure(DataError.Unexpected())
            viewModel.onSave()
            assertEquals(DeviceFormEvent.SaveFailed(FormError.UNEXPECTED), awaitItem())
        }
    }

    @Test
    fun `invalid fields are also reported as an event, for screen readers`() = runTest {
        repository.createResult = Result.failure(DataError.Invalid(mapOf(DeviceField.NAME to FieldError.REQUIRED)))
        val viewModel = newForm()

        viewModel.events.test {
            viewModel.onSave()
            assertEquals(DeviceFormEvent.SaveFailed(FormError.INVALID_FIELDS), awaitItem())
        }
    }

    @Test
    fun `save is ignored while a save is running`() = runTest {
        val viewModel = newForm()
        repository.saveGate = CompletableDeferred()

        viewModel.onSave()
        assertTrue(viewModel.uiState.value.isSaving)
        viewModel.onSave()
        repository.saveGate!!.complete(Unit)

        assertEquals(1, repository.savedInputs.size)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}
