package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.R
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.bodyMedium
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(1.dp, DeviceManagerTheme.extendedColors.surfaceStrong, CircleShape),
        shape = CircleShape,
        color = colors.surfaceContainerLow,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = textStyle.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcons.Search, contentDescription = null, tint = colors.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                placeholder,
                                style = textStyle,
                                color = DeviceManagerTheme.extendedColors.textSecondary,
                            )
                        }
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = AppIcons.Clear,
                                contentDescription = stringResource(R.string.designsystem_clear_search),
                                tint = DeviceManagerTheme.extendedColors.textSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.width(12.dp))
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun SearchFieldPreview() {
    DeviceManagerTheme {
        Surface {
            SearchField(query = "", onQueryChange = {}, placeholder = "Pesquisar", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview
@Composable
private fun SearchFieldFilledPreview() {
    DeviceManagerTheme {
        Surface {
            SearchField(query = "Câmera", onQueryChange = {
            }, placeholder = "Pesquisar", modifier = Modifier.padding(16.dp))
        }
    }
}
