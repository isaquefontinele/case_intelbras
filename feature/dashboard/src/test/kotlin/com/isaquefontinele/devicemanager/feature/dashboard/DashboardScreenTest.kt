package com.isaquefontinele.devicemanager.feature.dashboard

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val alarm =
        AlarmCentral(id = "a1", name = "Central de alarme", macAddress = "A0B1C2D3E4F5", password = "1234")
    private val camera = VideoDevice(
        id = "v1",
        name = "Câmera 1",
        serial = "ABCDE123",
        username = "admin",
        password = "pw",
        isFavorite = true,
    )
    private val devicesState = DashboardUiState(
        totalDevicesCount = 2,
        content = DashboardContent.Devices(listOf(alarm, camera)),
    )

    private fun setScreen(state: DashboardUiState, actions: DashboardActions = DashboardActions()) {
        composeRule.setContent { DeviceManagerTheme { DashboardScreen(state = state, actions = actions) } }
    }

    @Test
    fun `lists devices with the count, serial and formatted mac address`() {
        setScreen(devicesState)

        composeRule.onNodeWithText("2 dispositivos").assertExists()
        composeRule.onNodeWithText("A0:B1:C2:D3:E4:F5").assertExists()
        composeRule.onNodeWithText("ABCDE123").assertExists()
        composeRule.onNodeWithText("Todos").assertIsSelected()
    }

    @Test
    fun `row and actions button report the clicked device`() {
        val clicked = mutableListOf<Pair<String, Device>>()
        setScreen(
            devicesState,
            DashboardActions(
                onDeviceClick = { clicked += "row" to it },
                onShowActions = { clicked += "actions" to it },
            ),
        )

        composeRule.onNodeWithText("Central de alarme").performClick()
        composeRule.onNodeWithContentDescription("Ações de Câmera 1").performClick()

        assertEquals(listOf("row" to alarm, "actions" to camera), clicked)
    }

    @Test
    fun `empty state hides search and filters`() {
        setScreen(DashboardUiState(content = DashboardContent.Empty))

        composeRule.onNodeWithText("0 dispositivos").assertExists()
        composeRule.onNodeWithText("Nenhum dispositivo cadastrado").assertExists()
        composeRule.onNodeWithText("Pesquisar").assertDoesNotExist()
    }

    @Test
    fun `error state retries`() {
        var refreshes = 0
        setScreen(
            DashboardUiState(content = DashboardContent.Error(ErrorReason.NO_CONNECTION)),
            DashboardActions(onRefresh = { refreshes++ }),
        )

        composeRule.onNodeWithText("Sem conexão com o servidor.").assertExists()
        composeRule.onNodeWithText("Tentar novamente").performClick()

        assertEquals(1, refreshes)
    }

    @Test
    fun `no results state is shown when filters exclude everything`() {
        setScreen(devicesState.copy(content = DashboardContent.NoResults, filter = DeviceFilter.ALARMS))

        composeRule.onNodeWithText("Nenhum dispositivo encontrado").assertExists()
        composeRule.onNodeWithText("Alarmes").assertIsSelected()
    }

    @Test
    fun `removal dialog confirms and cancels`() {
        val calls = mutableListOf<String>()
        setScreen(
            devicesState.copy(removalDeviceCandidate = alarm),
            DashboardActions(onRemoveConfirm = { calls += "confirm" }, onRemoveDismiss = { calls += "dismiss" }),
        )

        composeRule.onNodeWithText("Remover dispositivo?").assertExists()
        composeRule.onNodeWithText("Confirmar").performClick()
        composeRule.onNodeWithText("Cancelar").performClick()

        assertEquals(listOf("confirm", "dismiss"), calls)
    }

    @Test
    fun `action sheet is titled with the device type and offers to unfavorite a favorite`() {
        val calls = mutableListOf<String>()
        composeRule.setContent {
            DeviceManagerTheme {
                DeviceActionsContent(
                    device = camera,
                    onInfo = { calls += "info" },
                    onEdit = { calls += "edit" },
                    onToggleFavorite = { calls += "favorite" },
                    onRemove = { calls += "remove" },
                )
            }
        }

        composeRule.onNodeWithText("Câmera").assertExists()
        composeRule.onNodeWithText("Marcar favorito").assertDoesNotExist()
        listOf("Informações", "Editar dispositivo", "Desmarcar favorito", "Remover dispositivo").forEach {
            composeRule.onNodeWithText(it).performClick()
        }

        assertEquals(listOf("info", "edit", "favorite", "remove"), calls)
    }
}
