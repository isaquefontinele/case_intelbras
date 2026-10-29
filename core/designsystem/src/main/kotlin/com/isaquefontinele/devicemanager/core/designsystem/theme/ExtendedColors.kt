package com.isaquefontinele.devicemanager.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Style guide tokens that have no direct slot in the Material 3 [androidx.compose.material3.ColorScheme]. */
@Immutable
data class ExtendedColors(
    val textSecondary: Color,
    val surfaceStrong: Color,
    val borderMedium: Color,
    val buttonBorder: Color,
)

internal val LightExtendedColors = ExtendedColors(
    textSecondary = InkSecondary,
    surfaceStrong = SurfaceStrong,
    borderMedium = BorderMedium,
    buttonBorder = ButtonBorder,
)

internal val DarkExtendedColors = ExtendedColors(
    textSecondary = InkSecondaryDark,
    surfaceStrong = SurfaceStrongDark,
    borderMedium = BorderMediumDark,
    buttonBorder = ButtonBorderDark,
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
