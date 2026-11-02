package com.isaquefontinele.devicemanager.feature.deviceform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isaquefontinele.devicemanager.core.designsystem.component.AppSnackbarHost
import com.isaquefontinele.devicemanager.core.designsystem.component.AppTextField
import com.isaquefontinele.devicemanager.core.designsystem.component.AppTopBar
import com.isaquefontinele.devicemanager.core.designsystem.component.LoadingIndicator
import com.isaquefontinele.devicemanager.core.designsystem.component.SegmentedSelector
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.core.model.DeviceType
import com.isaquefontinele.devicemanager.core.model.validation.DeviceField

@Composable
internal fun DeviceFormRoute(onClose: (DeviceFormResult?) -> Unit, viewModel: DeviceFormViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceFormEvent.Saved -> onClose(
                    when {
                        event.isQueued -> DeviceFormResult.SAVED_OFFLINE
                        event.isNew -> DeviceFormResult.ADDED
                        else -> DeviceFormResult.UPDATED
                    },
                )
                DeviceFormEvent.DeviceNotFound -> onClose(DeviceFormResult.NOT_FOUND)
                is DeviceFormEvent.SaveFailed ->
                    snackbarHostState.showSnackbar(resources.getString(event.error.messageRes()))
            }
        }
    }

    DeviceFormScreen(
        state = state,
        actions = DeviceFormActions(
            onClose = { onClose(null) },
            onSave = viewModel::onSave,
            onTypeChange = viewModel::onTypeChange,
            onNameChange = viewModel::onNameChange,
            onMacAddressChange = viewModel::onMacAddressChange,
            onSerialChange = viewModel::onSerialChange,
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
        ),
        snackbarHostState = snackbarHostState,
    )
}

internal data class DeviceFormActions(
    val onClose: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onTypeChange: (DeviceType) -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onMacAddressChange: (String) -> Unit = {},
    val onSerialChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
)

@Composable
internal fun DeviceFormScreen(
    state: DeviceFormUiState,
    actions: DeviceFormActions,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    title = stringResource(
                        if (state.isNew) R.string.deviceform_title_new else R.string.deviceform_title_edit,
                    ),
                    navigationIcon = AppIcons.Close,
                    navigationContentDescription = stringResource(R.string.deviceform_close),
                    onNavigationClick = actions.onClose,
                    actionText = stringResource(R.string.deviceform_save),
                    onActionClick = actions.onSave,
                    actionEnabled = !state.isSaving && !state.isLoading,
                )
                if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.isNew) TypeSelector(state.type, actions.onTypeChange)
                Fields(state, actions)
            }
        }
    }
}

@Composable
private fun TypeSelector(type: DeviceType, onTypeChange: (DeviceType) -> Unit) {
    Text(stringResource(R.string.deviceform_type), style = MaterialTheme.typography.titleSmall)
    val alarm = stringResource(R.string.deviceform_type_alarm)
    val video = stringResource(R.string.deviceform_type_video)
    SegmentedSelector(
        options = listOf(DeviceType.ALARM, DeviceType.VIDEO),
        selected = type,
        onSelect = onTypeChange,
        label = { if (it == DeviceType.ALARM) alarm else video },
    )
}

@Composable
private fun Fields(state: DeviceFormUiState, actions: DeviceFormActions) {
    @Composable
    fun errorFor(field: DeviceField): String? = state.errors[field]?.let { stringResource(it.messageRes(field)) }

    val next = KeyboardOptions(imeAction = ImeAction.Next)
    AppTextField(
        value = state.name,
        onValueChange = actions.onNameChange,
        label = stringResource(R.string.deviceform_name),
        errorText = errorFor(DeviceField.NAME),
        keyboardOptions = next.copy(capitalization = KeyboardCapitalization.Sentences),
    )
    when (state.type) {
        DeviceType.ALARM -> {
            AppTextField(
                value = state.macAddress,
                onValueChange = actions.onMacAddressChange,
                label = stringResource(R.string.deviceform_mac_address),
                errorText = errorFor(DeviceField.MAC_ADDRESS),
                keyboardOptions = next.copy(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    autoCorrectEnabled = false,
                ),
            )
            AppTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = stringResource(R.string.deviceform_password),
                errorText = errorFor(DeviceField.PASSWORD),
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
            )
        }
        DeviceType.VIDEO -> {
            AppTextField(
                value = state.serial,
                onValueChange = actions.onSerialChange,
                label = stringResource(R.string.deviceform_serial),
                errorText = errorFor(DeviceField.SERIAL),
                keyboardOptions = next.copy(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                ),
            )
            AppTextField(
                value = state.username,
                onValueChange = actions.onUsernameChange,
                label = stringResource(R.string.deviceform_username),
                errorText = errorFor(DeviceField.USERNAME),
                keyboardOptions = next.copy(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false),
            )
            AppTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = stringResource(R.string.deviceform_password),
                errorText = errorFor(DeviceField.PASSWORD),
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
        }
    }
}

@Preview
@Composable
private fun DeviceFormNewPreview() {
    DeviceManagerTheme { DeviceFormScreen(state = DeviceFormUiState(), actions = DeviceFormActions()) }
}

@Preview
@Composable
private fun DeviceFormEditAlarmPreview() {
    DeviceManagerTheme {
        DeviceFormScreen(
            state = DeviceFormUiState(
                isNew = false,
                type = DeviceType.ALARM,
                name = "Central de alarme",
                macAddress = "A0:B1:C2:D3:E4:F5",
                password = "123456",
            ),
            actions = DeviceFormActions(),
        )
    }
}
