package com.isaquefontinele.devicemanager.feature.deviceinfo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceInfoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val camera = VideoDevice("v1", "Câmera 1", "ABCDE123", "admin", "secret-password", isFavorite = true)
    private val alarm = AlarmCentral("a1", "Central", "A0B1C2D3E4F5", "1234")
    private val copied = mutableListOf<Pair<String, String>>()
    private var backs = 0

    private fun setScreen(state: DeviceInfoUiState) {
        composeRule.setContent {
            DeviceManagerTheme {
                DeviceInfoScreen(state = state, onBack = { backs++ }, onCopy = { label, value ->
                    copied +=
                        label to value
                })
            }
        }
    }

    @Test
    fun `camera shows type, serial and username but never the password`() {
        setScreen(DeviceInfoUiState.Content(camera))

        listOf("Câmera 1", "Favorito", "Vídeo", "ABCDE123", "admin").forEach {
            composeRule.onNodeWithText(it, useUnmergedTree = true).assertExists()
        }
        composeRule.onNodeWithText("secret-password", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `alarm shows the type and formatted mac address`() {
        setScreen(DeviceInfoUiState.Content(alarm))

        composeRule.onNodeWithText("Alarme", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("A0:B1:C2:D3:E4:F5", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Usuário", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText("Favorito", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `copy row copies the formatted value and announces the action`() {
        setScreen(DeviceInfoUiState.Content(alarm))

        val copyRow = composeRule.onNode(
            SemanticsMatcher("copy action") {
                it.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label ==
                    "Copiar MAC Address"
            },
        )
        copyRow.performClick()

        assertEquals(listOf("MAC Address" to "A0:B1:C2:D3:E4:F5"), copied)
    }

    @Test
    fun `pending change is shown only while it is pending`() {
        var device by mutableStateOf(camera.copy(isPendingSync = true))
        composeRule.setContent {
            DeviceManagerTheme {
                DeviceInfoScreen(state = DeviceInfoUiState.Content(device), onBack = {}, onCopy = { _, _ -> })
            }
        }

        composeRule.onNodeWithText("Aguardando envio", useUnmergedTree = true).assertExists()

        device = camera
        composeRule.onNodeWithText("Aguardando envio", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `back is reported`() {
        setScreen(DeviceInfoUiState.Content(camera))

        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `missing device shows a message`() {
        setScreen(DeviceInfoUiState.NotFound)

        composeRule.onNodeWithText("Este dispositivo não existe mais.").assertExists()
    }
}
