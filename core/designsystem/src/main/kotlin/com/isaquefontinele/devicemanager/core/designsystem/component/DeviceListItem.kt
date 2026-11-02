package com.isaquefontinele.devicemanager.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaquefontinele.devicemanager.core.designsystem.R
import com.isaquefontinele.devicemanager.core.designsystem.icon.AppIcons
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme

private val CardCornerRadius = 4.dp
private val CardShape = RoundedCornerShape(CardCornerRadius)

@Composable
fun ListCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.border(1.dp, DeviceManagerTheme.extendedColors.surfaceStrong, CardShape),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(content = content)
    }
}

/**
 * One row of a card that is split across lazy list items: draws only its slice of the card (rounded top on
 * the first row, rounded bottom on the last), so a [androidx.compose.foundation.lazy.LazyColumn] can render
 * the same look as [ListCard].
 */
@Composable
fun ListCardItem(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val background = MaterialTheme.colorScheme.surfaceContainerLow
    val border = DeviceManagerTheme.extendedColors.surfaceStrong
    Column(
        modifier = modifier.drawBehind {
            val radius = CardCornerRadius.toPx()
            val stroke = 1.dp.toPx()
            // Extend the rounded rect past the edges that continue in the neighbour rows, then clip.
            val top = if (isFirst) 0f else -2 * radius
            val bottom = if (isLast) size.height else size.height + 2 * radius
            clipRect {
                drawRoundRect(
                    color = background,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, bottom - top),
                    cornerRadius = CornerRadius(radius),
                )
                drawRoundRect(
                    color = border,
                    topLeft = Offset(stroke / 2, top + stroke / 2),
                    size = Size(size.width - stroke, bottom - top - stroke),
                    cornerRadius = CornerRadius(radius),
                    style = Stroke(stroke),
                )
            }
        },
        content = content,
    )
}

@Composable
fun ListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        color = DeviceManagerTheme.extendedColors.surfaceStrong,
    )
}

@Composable
fun DeviceTypeIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .border(1.dp, DeviceManagerTheme.extendedColors.surfaceStrong, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
fun DeviceListItem(
    name: String,
    detail: String,
    icon: ImageVector,
    isFavorite: Boolean,
    actionsContentDescription: String,
    onClick: () -> Unit,
    onActionsClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeviceTypeIcon(icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isFavorite) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = AppIcons.Favorited,
                        contentDescription = stringResource(R.string.designsystem_favorite),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (status != null) {
                Text(text = status, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
        }
        IconButton(onClick = onActionsClick) {
            Icon(AppIcons.More, contentDescription = actionsContentDescription)
        }
    }
}

@Preview
@Composable
private fun DeviceListPreview() {
    DeviceManagerTheme {
        Surface {
            ListCard(Modifier.padding(16.dp)) {
                DeviceListItem(
                    name = "Central de alarme",
                    detail = "A0:B1:C2:D3:E4:F5",
                    icon = AppIcons.Alarm,
                    isFavorite = true,
                    actionsContentDescription = "Ações",
                    onClick = {},
                    onActionsClick = {},
                )
                ListDivider()
                DeviceListItem(
                    name = "Câmera 1",
                    detail = "ABCDE12345678",
                    icon = AppIcons.Camera,
                    isFavorite = false,
                    actionsContentDescription = "Ações",
                    onClick = {},
                    onActionsClick = {},
                )
            }
        }
    }
}
