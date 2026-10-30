package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Text as drawn on screen, after the visual transformation. */
    private fun SemanticsNodeInteraction.renderedTexts(): List<String> {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.map { it.layoutInput.text.text }
    }

    @Test
    fun `password field is masked until the visibility button is pressed`() {
        composeRule.setContent {
            DeviceManagerTheme { AppTextField(value = "1234", onValueChange = {}, label = "Senha", isPassword = true) }
        }
        val field = composeRule.onNode(hasSetTextAction())

        assertFalse("1234" in field.renderedTexts())
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()

        assertTrue("1234" in field.renderedTexts())
        composeRule.onNodeWithContentDescription("Ocultar senha").assertExists()
    }

    @Test
    fun `text field shows the error text`() {
        composeRule.setContent {
            DeviceManagerTheme {
                AppTextField(value = "", onValueChange = {}, label = "Nome", errorText = "Campo obrigatório")
            }
        }

        composeRule.onNodeWithText("Campo obrigatório").assertExists()
    }

    @Test
    fun `segmented selector selects one option at a time`() {
        composeRule.setContent {
            var selected by remember { mutableStateOf("Vídeo") }
            DeviceManagerTheme {
                SegmentedSelector(
                    options = listOf("Alarme", "Vídeo"),
                    selected = selected,
                    onSelect = { selected = it },
                    label = { it },
                )
            }
        }

        composeRule.onNodeWithText("Vídeo").assertIsSelected()
        composeRule.onNodeWithText("Alarme").performClick()

        composeRule.onNodeWithText("Alarme").assertIsSelected()
        composeRule.onNodeWithText("Vídeo").assertIsNotSelected()
    }

    @Test
    fun `top bar reports navigation and action clicks`() {
        var navigation = 0
        var action = 0
        composeRule.setContent {
            DeviceManagerTheme {
                AppTopBar(
                    title = "Novo dispositivo",
                    navigationIcon = AppIcons.Close,
                    navigationContentDescription = "Fechar",
                    onNavigationClick = { navigation++ },
                    actionText = "Salvar",
                    onActionClick = { action++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Fechar").performClick()
        composeRule.onNodeWithText("Salvar").performClick()

        assertEquals(1, navigation)
        assertEquals(1, action)
    }

    @Test
    fun `confirm dialog reports confirm and dismiss`() {
        var confirmed = 0
        var dismissed = 0
        composeRule.setContent {
            DeviceManagerTheme {
                ConfirmDialog(
                    title = "Remover dispositivo?",
                    text = "Você tem certeza que deseja remover este dispositivo?",
                    confirmText = "Confirmar",
                    dismissText = "Cancelar",
                    onConfirm = { confirmed++ },
                    onDismiss = { dismissed++ },
                )
            }
        }

        composeRule.onNodeWithText("Confirmar").performClick()
        composeRule.onNodeWithText("Cancelar").performClick()

        assertEquals(1, confirmed)
        assertEquals(1, dismissed)
    }

    @Test
    fun `snackbar host shows the message`() {
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) { hostState.showSnackbar("Dispositivo removido") }
            DeviceManagerTheme { AppSnackbarHost(hostState) }
        }

        composeRule.onNodeWithText("Dispositivo removido").assertExists()
    }
}
