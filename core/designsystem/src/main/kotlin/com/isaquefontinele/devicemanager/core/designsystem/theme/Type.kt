package com.isaquefontinele.devicemanager.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.isaquefontinele.devicemanager.core.designsystem.R

@OptIn(ExperimentalTextApi::class)
private fun openSans(weight: FontWeight) = Font(
    resId = R.font.open_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

// Style guide: Open Sans only, Regular (400) and SemiBold (600).
val OpenSans = FontFamily(
    openSans(FontWeight.Normal),
    openSans(FontWeight.SemiBold),
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = OpenSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

// Sizes measured from the 360dp wireframe frames.
internal val AppTypography = Typography(
    headlineSmall = style(size = 24, lineHeight = 32, weight = FontWeight.SemiBold),
    titleLarge = style(size = 22, lineHeight = 28, weight = FontWeight.SemiBold),
    titleMedium = style(size = 16, lineHeight = 24, weight = FontWeight.SemiBold),
    titleSmall = style(size = 14, lineHeight = 20, weight = FontWeight.SemiBold),
    bodyLarge = style(size = 16, lineHeight = 24),
    bodyMedium = style(size = 14, lineHeight = 20),
    bodySmall = style(size = 12, lineHeight = 16),
    labelLarge = style(size = 14, lineHeight = 20, weight = FontWeight.SemiBold),
    labelMedium = style(size = 12, lineHeight = 16, weight = FontWeight.SemiBold),
    labelSmall = style(size = 11, lineHeight = 16),
)
