package com.isaquefontinele.devicemanager.feature.deviceform

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField
import com.isaquefontinele.devicemanager.core.model.validation.FieldError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceFormScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(state: DeviceFormUiState, actions: DeviceFormActions = DeviceFormActions()) {
        composeRule.setContent { DeviceManagerTheme { DeviceFormScreen(state = state, actions = actions) } }
    }

    @Test
    fun `new video form shows the type selector and the video fields`() {
        setScreen(DeviceFormUiState())

        composeRule.onNodeWithText("Novo dispositivo").assertExists()
        composeRule.onNodeWithText("Vídeo").assertIsSelected()
        listOf("Nome", "Número de série", "Usuário", "Senha").forEach { composeRule.onNodeWithText(it).assertExists() }
        composeRule.onNodeWithText("MAC Address").assertDoesNotExist()
    }

    @Test
    fun `alarm form shows the mac address instead of serial and username`() {
        setScreen(DeviceFormUiState(type = DeviceType.ALARM))

        composeRule.onNodeWithText("MAC Address").assertExists()
        composeRule.onNodeWithText("Número de série").assertDoesNotExist()
        composeRule.onNodeWithText("Usuário").assertDoesNotExist()
    }

    @Test
    fun `edit form has no type selector`() {
        setScreen(DeviceFormUiState(isNew = false, type = DeviceType.ALARM, name = "Casa"))

        composeRule.onNodeWithText("Editar dispositivo").assertExists()
        composeRule.onNodeWithText("Tipo de dispositivo").assertDoesNotExist()
    }

    @Test
    fun `field errors are shown under their fields`() {
        setScreen(
            DeviceFormUiState(
                type = DeviceType.ALARM,
                errors = mapOf(
                    DeviceField.NAME to FieldError.REQUIRED,
                    DeviceField.MAC_ADDRESS to FieldError.ALREADY_IN_USE,
                    DeviceField.PASSWORD to FieldError.INVALID_ALARM_PASSWORD,
                ),
            ),
        )

        composeRule.onNodeWithText("Campo obrigatório").assertExists()
        composeRule.onNodeWithText("Este MAC Address já está cadastrado").assertExists()
        composeRule.onNodeWithText("A senha deve ter 4 ou 6 dígitos").assertExists()
    }

    @Test
    fun `length errors are shown under their fields`() {
        setScreen(
            DeviceFormUiState(
                type = DeviceType.VIDEO,
                errors = mapOf(
                    DeviceField.NAME to FieldError.TOO_LONG,
                    DeviceField.SERIAL to FieldError.INVALID_SERIAL_LENGTH,
                ),
            ),
        )

        composeRule.onNodeWithText("Use no máximo 200 caracteres").assertExists()
        composeRule.onNodeWithText("O número de série deve ter 13 caracteres").assertExists()
    }

    @Test
    fun `typing, switching type, saving and closing are reported`() {
        val calls = mutableListOf<String>()
        setScreen(
            DeviceFormUiState(),
            DeviceFormActions(
                onNameChange = { calls += "name:$it" },
                onTypeChange = { calls += "type:$it" },
                onSave = { calls += "save" },
                onClose = { calls += "close" },
            ),
        )

        composeRule.onNodeWithText("Nome").performTextInput("Porta")
        composeRule.onNodeWithText("Alarme").performClick()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithContentDescription("Fechar").performClick()

        assertEquals(listOf("name:Porta", "type:ALARM", "save", "close"), calls)
    }

    @Test
    fun `save is disabled while saving`() {
        setScreen(DeviceFormUiState(isSaving = true))

        composeRule.onNodeWithText("Salvar").assertIsNotEnabled()
    }
}
