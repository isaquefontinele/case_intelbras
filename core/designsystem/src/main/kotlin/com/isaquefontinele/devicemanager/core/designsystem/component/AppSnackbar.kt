package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        AppSnackbar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onAction = data::performAction,
        )
    }
}

@Composable
private fun AppSnackbar(message: String, actionLabel: String?, onAction: () -> Unit) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(4.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        action = actionLabel?.let {
            {
                TextButton(onClick = onAction) {
                    Text(it, color = MaterialTheme.colorScheme.inversePrimary)
                }
            }
        },
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun AppSnackbarPreview() {
    DeviceManagerTheme {
        AppSnackbar(message = "Dispositivo removido", actionLabel = null, onAction = {})
    }
}
