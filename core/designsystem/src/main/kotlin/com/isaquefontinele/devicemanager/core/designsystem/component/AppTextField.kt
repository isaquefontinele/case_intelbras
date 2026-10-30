package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.R
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val secondary = DeviceManagerTheme.extendedColors.textSecondary

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        textStyle = MaterialTheme.typography.bodyLarge,

        keyboardOptions = if (isPassword && keyboardOptions.keyboardType == KeyboardType.Unspecified) {
            keyboardOptions.copy(keyboardType = KeyboardType.Password)
        } else {
            keyboardOptions
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            { PasswordVisibilityToggle(visible = passwordVisible, onToggle = { passwordVisible = !passwordVisible }) }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = secondary,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = secondary,
            focusedTrailingIconColor = secondary,
            unfocusedTrailingIconColor = secondary,
            errorTrailingIconColor = secondary,
        ),
    )
}

@Composable
private fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    val description = if (visible) R.string.designsystem_hide_password else R.string.designsystem_show_password
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) AppIcons.Visibility else AppIcons.VisibilityOff,
            contentDescription = stringResource(description),
        )
    }
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    DeviceManagerTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(value = "", onValueChange = {}, label = "Nome")
                AppTextField(value = "A0:B1:C2:D3:E4:F5", onValueChange = {}, label = "MAC Address")
                AppTextField(value = "123", onValueChange = {
                }, label = "Senha", isPassword = true, errorText = "Use 4 ou 6 dígitos")
            }
        }
    }
}
