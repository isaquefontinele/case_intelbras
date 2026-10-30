package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun AppTopBar(
    title: String,
    navigationIcon: ImageVector,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
    actionEnabled: Boolean = true,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigationClick) {
                Icon(navigationIcon, contentDescription = navigationContentDescription)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .semantics { heading() },
            )
            if (actionText != null) {
                TextButton(onClick = onActionClick, enabled = actionEnabled) {
                    Text(actionText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppTopBarPreview() {
    DeviceManagerTheme {
        Column {
            AppTopBar(
                title = "Novo dispositivo",
                navigationIcon = AppIcons.Close,
                navigationContentDescription = "Fechar",
                onNavigationClick = {},
                actionText = "Salvar",
            )
            AppTopBar(
                title = "Informações",
                navigationIcon = AppIcons.Back,
                navigationContentDescription = "Voltar",
                onNavigationClick = {},
            )
        }
    }
}
