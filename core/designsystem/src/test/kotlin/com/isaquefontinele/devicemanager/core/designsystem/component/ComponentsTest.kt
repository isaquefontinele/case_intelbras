package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `search field shows the clear button only with text and clears the query`() {
        composeRule.setContent {
            var query by remember { mutableStateOf("") }
            DeviceManagerTheme { SearchField(query = query, onQueryChange = { query = it }, placeholder = "Pesquisar") }
        }

        composeRule.onNodeWithContentDescription("Limpar pesquisa").assertDoesNotExist()
        composeRule.onNodeWithText("Pesquisar").performTextInput("Câmera")
        composeRule.onNodeWithText("Câmera").assertExists()

        composeRule.onNodeWithContentDescription("Limpar pesquisa").performClick()

        composeRule.onNodeWithText("Câmera").assertDoesNotExist()
        composeRule.onNodeWithText("Pesquisar").assertExists()
    }

    @Test
    fun `filter chip exposes its selected state`() {
        composeRule.setContent {
            var selected by remember { mutableStateOf("Todos") }
            DeviceManagerTheme {
                listOf("Todos", "Favoritos").forEach { label ->
                    AppFilterChip(label = label, selected = label == selected, onClick = { selected = label })
                }
            }
        }

        composeRule.onNodeWithText("Todos").assertIsSelected()
        composeRule.onNodeWithText("Favoritos").assertIsNotSelected().performClick()

        composeRule.onNodeWithText("Favoritos").assertIsSelected()
        composeRule.onNodeWithText("Todos").assertIsNotSelected()
    }

    @Test
    fun `device list item reports row and actions clicks separately`() {
        var rowClicks = 0
        var actionClicks = 0
        composeRule.setContent {
            DeviceManagerTheme {
                DeviceListItem(
                    name = "Câmera 1",
                    detail = "ABC123",
                    icon = AppIcons.Camera,
                    isFavorite = true,
                    actionsContentDescription = "Ações de Câmera 1",
                    onClick = { rowClicks++ },
                    onActionsClick = { actionClicks++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Favorito").assertExists()
        composeRule.onNodeWithText("Câmera 1").performClick()
        composeRule.onNodeWithContentDescription("Ações de Câmera 1").performClick()

        assertEquals(1, rowClicks)
        assertEquals(1, actionClicks)
    }

    @Test
    fun `favorite star is hidden for regular devices`() {
        composeRule.setContent {
            DeviceManagerTheme {
                DeviceListItem(
                    name = "Câmera 1",
                    detail = "ABC123",
                    icon = AppIcons.Camera,
                    isFavorite = false,
                    actionsContentDescription = "Ações",
                    onClick = {},
                    onActionsClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Favorito").assertDoesNotExist()
    }
}
