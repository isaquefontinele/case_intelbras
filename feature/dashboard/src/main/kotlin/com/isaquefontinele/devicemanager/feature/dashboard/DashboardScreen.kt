package com.isaquefontinele.devicemanager.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isaquefontinele.devicemanager.core.designsystem.component.AppFilterChip
import com.isaquefontinele.devicemanager.core.designsystem.component.AppSnackbarHost
import com.isaquefontinele.devicemanager.core.designsystem.component.ConfirmDialog
import com.isaquefontinele.devicemanager.core.designsystem.component.DeviceListItem
import com.isaquefontinele.devicemanager.core.designsystem.component.ListCardItem
import com.isaquefontinele.devicemanager.core.designsystem.component.ListDivider
import com.isaquefontinele.devicemanager.core.designsystem.component.PillButton
import com.isaquefontinele.devicemanager.core.designsystem.component.PillButtonStyle
import com.isaquefontinele.devicemanager.core.designsystem.component.SearchField
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.AlarmCentral
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.MacAddress
import com.isaquefontinele.devicemanager.core.model.VideoDevice
import com.isaquefontinele.devicemanager.feature.dashboard.navigation.DashboardNotice
import kotlinx.coroutines.launch

@Composable
internal fun DashboardRoute(
    onAddDevice: () -> Unit,
    onDeviceInfo: (Device) -> Unit,
    onEditDevice: (Device) -> Unit,
    notice: DashboardNotice? = null,
    onNoticeShown: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(resources.getString(it.messageRes())) }
    }
    NoticeEffect(notice, onNoticeShown, snackbarHostState)

    DashboardScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = DashboardActions(
            onQueryChange = viewModel::onQueryChange,
            onFilterChange = viewModel::onFilterChange,
            onRefresh = viewModel::refresh,
            onAddDevice = onAddDevice,
            onDeviceClick = onDeviceInfo,
            onShowActions = viewModel::onShowActions,
            onDismissActions = viewModel::onDismissActions,
            onInfo = { device ->
                viewModel.onDismissActions()
                onDeviceInfo(device)
            },
            onEdit = { device ->
                viewModel.onDismissActions()
                onEditDevice(device)
            },
            onToggleFavorite = viewModel::onToggleFavorite,
            onRemoveRequest = viewModel::onRemoveRequest,
            onRemoveConfirm = viewModel::onRemoveConfirm,
            onRemoveDismiss = viewModel::onRemoveDismiss,
        ),
    )
}

/**
 * Shows [notice] once. The snackbar runs outside the effect: consuming the notice changes the effect's key, which
 * would cancel a snackbar shown from inside it.
 */
@Composable
internal fun NoticeEffect(notice: DashboardNotice?, onNoticeShown: () -> Unit, snackbarHostState: SnackbarHostState) {
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(notice) {
        if (notice != null) {
            onNoticeShown()
            scope.launch { snackbarHostState.showSnackbar(resources.getString(notice.messageRes())) }
        }
    }
}

internal data class DashboardActions(
    val onQueryChange: (String) -> Unit = {},
    val onFilterChange: (DeviceFilter) -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onAddDevice: () -> Unit = {},
    val onDeviceClick: (Device) -> Unit = {},
    val onShowActions: (Device) -> Unit = {},
    val onDismissActions: () -> Unit = {},
    val onInfo: (Device) -> Unit = {},
    val onEdit: (Device) -> Unit = {},
    val onToggleFavorite: (Device) -> Unit = {},
    val onRemoveRequest: (Device) -> Unit = {},
    val onRemoveConfirm: () -> Unit = {},
    val onRemoveDismiss: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(
    state: DashboardUiState,
    actions: DashboardActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            PillButton(
                text = stringResource(R.string.dashboard_add),
                onClick = actions.onAddDevice,
                style = PillButtonStyle.Neutral,
                icon = AppIcons.Add,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing && state.content !is DashboardContent.Loading,
            onRefresh = actions.onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            ) {
                item(key = "header") { Header(state.totalDevicesCount) }
                if (state.totalDevicesCount > 0) {
                    item(key = "filters") { Filters(state, actions) }
                }
                when (val content = state.content) {
                    DashboardContent.Loading -> item(key = "loading") { Loading() }
                    is DashboardContent.Error -> item(key = "error") {
                        Message(
                            title = stringResource(R.string.dashboard_error_title),
                            text = stringResource(content.reason.messageRes()),
                            action = stringResource(R.string.dashboard_retry) to actions.onRefresh,
                        )
                    }
                    DashboardContent.Empty -> item(key = "empty") {
                        Message(
                            title = stringResource(R.string.dashboard_empty_title),
                            text = stringResource(R.string.dashboard_empty_text),
                        )
                    }
                    DashboardContent.NoResults -> item(key = "no-results") {
                        Message(
                            title = stringResource(R.string.dashboard_no_results_title),
                            text = stringResource(R.string.dashboard_no_results_text),
                        )
                    }
                    is DashboardContent.Devices -> itemsIndexed(
                        items = content.devices,
                        key = { _, device -> device.id },
                    ) { index, device ->
                        DeviceRow(
                            device = device,
                            isFirst = index == 0,
                            isLast = index == content.devices.lastIndex,
                            actions = actions,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    state.selectedDevice?.let { device ->
        DeviceActionsSheet(
            device = device,
            onDismiss = actions.onDismissActions,
            onInfo = { actions.onInfo(device) },
            onEdit = { actions.onEdit(device) },
            onToggleFavorite = { actions.onToggleFavorite(device) },
            onRemove = { actions.onRemoveRequest(device) },
        )
    }
    if (state.removalDeviceCandidate != null) {
        ConfirmDialog(
            title = stringResource(R.string.dashboard_remove_title),
            text = stringResource(R.string.dashboard_remove_text),
            confirmText = stringResource(R.string.dashboard_remove_confirm),
            dismissText = stringResource(R.string.dashboard_remove_cancel),
            onConfirm = actions.onRemoveConfirm,
            onDismiss = actions.onRemoveDismiss,
        )
    }
}

@Composable
private fun Header(totalCount: Int) {
    Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            // Portuguese treats 0 as singular ("0 dispositivo"), so zero has its own string.
            text = if (totalCount == 0) {
                stringResource(R.string.dashboard_device_count_zero)
            } else {
                pluralStringResource(R.plurals.dashboard_device_count, totalCount, totalCount)
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Filters(state: DashboardUiState, actions: DashboardActions) {
    Column(Modifier.padding(bottom = 12.dp)) {
        SearchField(
            query = state.query,
            onQueryChange = actions.onQueryChange,
            placeholder = stringResource(R.string.dashboard_search),
        )
        LazyRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(DeviceFilter.entries.size) { index ->
                val filter = DeviceFilter.entries[index]
                AppFilterChip(
                    label = stringResource(filter.labelRes()),
                    selected = filter == state.filter,
                    onClick = { actions.onFilterChange(filter) },
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    isFirst: Boolean,
    isLast: Boolean,
    actions: DashboardActions,
    modifier: Modifier = Modifier,
) {
    ListCardItem(isFirst = isFirst, isLast = isLast, modifier = modifier) {
        DeviceListItem(
            name = device.name,
            detail = when (device) {
                is VideoDevice -> device.serial
                is AlarmCentral -> MacAddress.format(device.macAddress)
            },
            icon = if (device is VideoDevice) AppIcons.Camera else AppIcons.Alarm,
            isFavorite = device.isFavorite,
            actionsContentDescription = stringResource(R.string.dashboard_device_actions, device.name),
            onClick = { actions.onDeviceClick(device) },
            onActionsClick = { actions.onShowActions(device) },
            status = if (device.isPendingSync) stringResource(R.string.dashboard_pending_sync) else null,
        )
        if (!isLast) ListDivider()
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Message(title: String, text: String, action: Pair<String, () -> Unit>? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = DeviceManagerTheme.extendedColors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            PillButton(text = action.first, onClick = action.second, style = PillButtonStyle.Outlined)
        }
    }
}

private val previewDevices = listOf(
    AlarmCentral(
        id = "a1",
        name = "Central de alarme",
        macAddress = "A0B1C2D3E4F5",
        password = "1234",
        isFavorite = true,
    ),
    VideoDevice(id = "v1", name = "Câmera 1", serial = "ABCDE12345678", username = "admin", password = "pw"),
    VideoDevice(id = "v2", name = "Câmera 2", serial = "ABCDE12345678", username = "admin", password = "pw"),
)

@Preview
@Composable
private fun DashboardScreenPreview() {
    DeviceManagerTheme {
        DashboardScreen(
            state = DashboardUiState(totalDevicesCount = 3, content = DashboardContent.Devices(previewDevices)),
            actions = DashboardActions(),
        )
    }
}

@Preview
@Composable
private fun DashboardEmptyPreview() {
    DeviceManagerTheme {
        DashboardScreen(state = DashboardUiState(content = DashboardContent.Empty), actions = DashboardActions())
    }
}
