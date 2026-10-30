@file:Suppress("MatchingDeclarationName") // The file is named after the composable, not its style enum.

package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

enum class PillButtonStyle {
    Confirmation,
    Neutral,
    Cancellation,
    Outlined,
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillButtonStyle = PillButtonStyle.Confirmation,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val extended = DeviceManagerTheme.extendedColors
    val (container, content) = when (style) {
        PillButtonStyle.Confirmation -> colors.primary to colors.onPrimary
        PillButtonStyle.Neutral -> extended.surfaceStrong to colors.onSurface
        PillButtonStyle.Cancellation -> colors.error to colors.onError
        PillButtonStyle.Outlined -> Color.Transparent to colors.onSurface
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        border = if (style == PillButtonStyle.Outlined) BorderStroke(1.dp, extended.buttonBorder) else null,
        elevation = null,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (style == PillButtonStyle.Neutral) FontWeight.Normal else FontWeight.SemiBold,
        )
    }
}

@Preview
@Composable
private fun PillButtonPreview() {
    DeviceManagerTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PillButton(text = "Adicionar", onClick = {}, style = PillButtonStyle.Neutral, icon = AppIcons.Add)
                PillButton(text = "Salvar", onClick = {})
                PillButton(text = "Confirmar", onClick = {}, style = PillButtonStyle.Cancellation)
                PillButton(text = "Cancelar", onClick = {}, style = PillButtonStyle.Outlined)
            }
        }
    }
}
