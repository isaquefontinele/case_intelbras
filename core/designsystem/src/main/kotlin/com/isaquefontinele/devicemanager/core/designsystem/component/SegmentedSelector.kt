package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun <T> SegmentedSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = DeviceManagerTheme.extendedColors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = extended.surfaceStrong,
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .selectableGroup(),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) }),
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) colors.background else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, extended.borderMedium) else null,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) colors.primary else colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedSelectorPreview() {
    DeviceManagerTheme {
        Surface {
            SegmentedSelector(
                options = listOf("Alarme", "Vídeo"),
                selected = "Vídeo",
                onSelect = {},
                label = { it },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
