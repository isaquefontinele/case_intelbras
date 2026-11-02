package com.isaquefontinele.devicemanager.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.isaquefontinele.devicemanager.core.designsystem.R

/**
 * Every icon used by the app, so swapping them only touches this file and its drawables.
 */
object AppIcons {
    val Close: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_close)

    val Back: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_back)

    val Search: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_search)

    val Clear: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_clear)

    val Add: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_add)

    val More: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_more)

    val Info: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_info)

    val Edit: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_edit)

    val Delete: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_delete)

    val Visibility: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_visibility)

    val VisibilityOff: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_visibility_off)

    val Copy: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_copy)

    val Alarm: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_alarm)

    val Camera: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_camera)

    val Favorite: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_favorite)

    val Favorited: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_favorited)

    val Unfavorite: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_unfavorite)
}
