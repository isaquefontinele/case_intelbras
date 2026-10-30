package com.isaquefontinele.devicemanager.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceActionsSheet(
    device: Device,
    onDismiss: () -> Unit,
    onInfo: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background) {
        DeviceActionsContent(device, onInfo, onEdit, onToggleFavorite, onRemove)
    }
}

@Composable
internal fun DeviceActionsContent(
    device: Device,
    onInfo: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(
            text = stringResource(device.type.labelRes()),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .semantics { heading() },
        )
        SheetAction(AppIcons.Info, stringResource(R.string.dashboard_action_info), onInfo)
        SheetAction(AppIcons.Edit, stringResource(R.string.dashboard_action_edit), onEdit)
        if (device.isFavorite) {
            SheetAction(AppIcons.Favorited, stringResource(R.string.dashboard_action_unfavorite), onToggleFavorite)
        } else {
            SheetAction(AppIcons.Favorite, stringResource(R.string.dashboard_action_favorite), onToggleFavorite)
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = DeviceManagerTheme.extendedColors.borderMedium,
        )
        SheetAction(
            icon = AppIcons.Delete,
            label = stringResource(R.string.dashboard_action_remove),
            onClick = onRemove,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, onClick: () -> Unit, color: Color = Color.Unspecified) {
    val contentColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = contentColor)
    }
}

internal fun DeviceType.labelRes(): Int = when (this) {
    DeviceType.VIDEO -> R.string.dashboard_type_camera
    DeviceType.ALARM -> R.string.dashboard_type_alarm
}
