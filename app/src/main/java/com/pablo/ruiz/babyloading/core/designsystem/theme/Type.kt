package com.pablo.ruiz.babyloading.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pablo.ruiz.babyloading.R

@OptIn(ExperimentalTextApi::class)
private fun nunitoSansFont(
    weight: FontWeight,
    style: FontStyle = FontStyle.Normal,
) = Font(
    resId = if (style == FontStyle.Italic) {
        R.font.nunito_sans_italic_variable
    } else {
        R.font.nunito_sans_variable
    },
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
    ),
)

/**
 * Local Nunito Sans variable-font family. Every supported weight has a real italic counterpart;
 * callers must use [FontStyle.Italic] instead of relying on synthetic slanting.
 */
internal val BabyLoadingFontFamily = FontFamily(
    nunitoSansFont(FontWeight.Normal),
    nunitoSansFont(FontWeight.Medium),
    nunitoSansFont(FontWeight.SemiBold),
    nunitoSansFont(FontWeight.Bold),
    nunitoSansFont(FontWeight.ExtraBold),
    nunitoSansFont(FontWeight.Normal, FontStyle.Italic),
    nunitoSansFont(FontWeight.Medium, FontStyle.Italic),
    nunitoSansFont(FontWeight.SemiBold, FontStyle.Italic),
    nunitoSansFont(FontWeight.Bold, FontStyle.Italic),
    nunitoSansFont(FontWeight.ExtraBold, FontStyle.Italic),
)

private val MaterialDefaultTypography = Typography()

private fun TextStyle.withBabyLoadingFont(weight: FontWeight) = copy(
    fontFamily = BabyLoadingFontFamily,
    fontWeight = weight,
)

val BabyLoadingTypography = Typography(
    displayLarge = MaterialDefaultTypography.displayLarge.withBabyLoadingFont(FontWeight.ExtraBold),
    displayMedium = MaterialDefaultTypography.displayMedium.withBabyLoadingFont(FontWeight.ExtraBold),
    displaySmall = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = MaterialDefaultTypography.headlineSmall.withBabyLoadingFont(FontWeight.Bold),
    titleLarge = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = MaterialDefaultTypography.titleSmall.withBabyLoadingFont(FontWeight.Bold),
    bodyLarge = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = MaterialDefaultTypography.bodySmall.withBabyLoadingFont(FontWeight.Normal),
    labelLarge = TextStyle(
        fontFamily = BabyLoadingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = MaterialDefaultTypography.labelMedium.withBabyLoadingFont(FontWeight.SemiBold),
    labelSmall = MaterialDefaultTypography.labelSmall.withBabyLoadingFont(FontWeight.SemiBold),
)
