package com.isaquefontinele.devicemanager.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = Green,
    secondaryContainer = GreenSoft,
    onSecondaryContainer = Green,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = SurfaceStrong,
    onSurfaceVariant = Ink,
    surfaceContainerLowest = White,
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Surface,
    surfaceContainerHighest = SurfaceStrong,
    outline = InkSecondary,
    outlineVariant = BorderMedium,
    error = Red,
    onError = White,
    inverseSurface = DarkSurface,
    inverseOnSurface = OnDarkSurface,
    inversePrimary = GreenDark,
)

private val DarkColors = darkColorScheme(
    primary = GreenDark,
    onPrimary = BackgroundDark,
    primaryContainer = GreenSoftDark,
    onPrimaryContainer = GreenDark,
    secondaryContainer = GreenSoftDark,
    onSecondaryContainer = GreenDark,
    background = BackgroundDark,
    onBackground = InkDark,
    surface = BackgroundDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceStrongDark,
    onSurfaceVariant = InkDark,
    surfaceContainerLowest = BackgroundDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceDark,
    surfaceContainerHighest = SurfaceStrongDark,
    outline = InkSecondaryDark,
    outlineVariant = BorderMediumDark,
    error = RedDark,
    onError = BackgroundDark,
    inverseSurface = OnDarkSurface,
    inverseOnSurface = DarkSurface,
    inversePrimary = Green,
)

@Composable
fun DeviceManagerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme: ColorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

/** Access to the style guide tokens that are not part of [MaterialTheme.colorScheme]. */
object DeviceManagerTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
