package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        ConfirmDialogContent(title, text, confirmText, dismissText, onConfirm, onDismiss)
    }
}

@Composable
private fun ConfirmDialogContent(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PillButton(text = confirmText, onClick = onConfirm, style = PillButtonStyle.Cancellation)
                PillButton(text = dismissText, onClick = onDismiss, style = PillButtonStyle.Outlined)
            }
        }
    }
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    DeviceManagerTheme {
        ConfirmDialogContent(
            title = "Remover dispositivo?",
            text = "Você tem certeza que deseja remover este dispositivo?",
            confirmText = "Confirmar",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
