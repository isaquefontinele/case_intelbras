package com.isaquefontinele.devicemanager.feature.deviceinfo

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isaquefontinele.devicemanager.core.designsystem.component.AppSnackbarHost
import com.isaquefontinele.devicemanager.core.designsystem.component.AppTopBar
import com.isaquefontinele.devicemanager.core.designsystem.component.DeviceTypeIcon
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.MacAddress
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(4.dp)

@Composable
internal fun DeviceInfoRoute(onBack: () -> Unit, viewModel: DeviceInfoViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()

    DeviceInfoScreen(
        state = state,
        onBack = onBack,
        onCopy = { label, value ->
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, value)))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    snackbarHostState.showSnackbar(resources.getString(R.string.deviceinfo_copied))
                }
            }
        },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun DeviceInfoScreen(
    state: DeviceInfoUiState,
    onBack: () -> Unit,
    onCopy: (label: String, value: String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.deviceinfo_title),
                navigationIcon = AppIcons.Back,
                navigationContentDescription = stringResource(R.string.deviceinfo_back),
                onNavigationClick = onBack,
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                DeviceInfoUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DeviceInfoUiState.NotFound -> Text(
                    text = stringResource(R.string.deviceinfo_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                )
                is DeviceInfoUiState.Content -> Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeaderCard(state.device)
                    DetailsCard(state.device, onCopy)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeviceManagerTheme.extendedColors.surfaceStrong, CardShape),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        content = content,
    )
}

@Composable
private fun HeaderCard(device: Device) {
    InfoCard {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            DeviceTypeIcon(if (device is VideoDevice) AppIcons.Camera else AppIcons.Alarm)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(device.name, style = MaterialTheme.typography.titleSmall)
                if (device.isFavorite) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Favorited,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.deviceinfo_favorite), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsCard(device: Device, onCopy: (String, String) -> Unit) {
    InfoCard {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            when (device) {
                is VideoDevice -> {
                    DetailRow(stringResource(R.string.deviceinfo_type), stringResource(R.string.deviceinfo_type_video))
                    CopyableRow(stringResource(R.string.deviceinfo_serial), device.serial, onCopy)
                    DetailRow(stringResource(R.string.deviceinfo_username), device.username)
                }
                is AlarmCentral -> {
                    DetailRow(stringResource(R.string.deviceinfo_type), stringResource(R.string.deviceinfo_type_alarm))
                    CopyableRow(
                        stringResource(R.string.deviceinfo_mac_address),
                        MacAddress.format(device.macAddress),
                        onCopy,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

@Composable
private fun CopyableRow(label: String, value: String, onCopy: (String, String) -> Unit) {
    DetailRow(
        label = label,
        value = value,
        modifier = Modifier.clickable(
            onClickLabel = stringResource(R.string.deviceinfo_copy, label),
            role = Role.Button,
            onClick = { onCopy(label, value) },
        ),
    ) {
        Spacer(Modifier.width(4.dp))
        Icon(AppIcons.Copy, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Preview
@Composable
private fun DeviceInfoCameraPreview() {
    DeviceManagerTheme {
        DeviceInfoScreen(
            state = DeviceInfoUiState.Content(
                VideoDevice("v1", "Câmera 1", "ABCDE12345678", "admin", "pw", isFavorite = true),
            ),
            onBack = {},
            onCopy = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun DeviceInfoAlarmPreview() {
    DeviceManagerTheme {
        DeviceInfoScreen(
            state = DeviceInfoUiState.Content(AlarmCentral("a1", "Central de alarme", "A0B1C2D3E4F5", "1234")),
            onBack = {},
            onCopy = { _, _ -> },
        )
    }
}
