package com.pablo.ruiz.babyloading.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

internal val BabyLoadingColorScheme = lightColorScheme(
    primary = BrandPink,
    onPrimary = OnBrandPink,
    primaryContainer = BrandPinkContainer,
    onPrimaryContainer = OnBrandPinkContainer,
    secondary = BrandLavender,
    onSecondary = OnBrandLavender,
    secondaryContainer = BrandLavenderContainer,
    onSecondaryContainer = OnBrandLavenderContainer,
    tertiary = WarmTertiary,
    onTertiary = OnWarmTertiary,
    tertiaryContainer = WarmTertiaryContainer,
    onTertiaryContainer = OnWarmTertiaryContainer,
    error = BabyError,
    onError = BabyOnError,
    errorContainer = BabyErrorContainer,
    onErrorContainer = BabyOnErrorContainer,
    background = BabyBackground,
    onBackground = BabyOnSurface,
    surface = BabySurface,
    onSurface = BabyOnSurface,
    surfaceVariant = BabySurfaceVariant,
    onSurfaceVariant = BabyOnSurfaceVariant,
    outline = BabyOutline,
    outlineVariant = BabyOutlineVariant,
)

@Composable
fun BabyLoadingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BabyLoadingColorScheme,
        typography = BabyLoadingTypography,
        shapes = BabyLoadingShapes,
        content = content,
    )
}
