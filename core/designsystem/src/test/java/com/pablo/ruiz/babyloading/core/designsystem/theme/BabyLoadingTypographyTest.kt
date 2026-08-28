package com.pablo.ruiz.babyloading.core.designsystem.theme

import org.junit.Assert.assertSame
import org.junit.Test

class BabyLoadingTypographyTest {
    @Test
    fun everyMaterialTypographyRoleUsesNunitoSans() {
        val styles = listOf(
            BabyLoadingTypography.displayLarge,
            BabyLoadingTypography.displayMedium,
            BabyLoadingTypography.displaySmall,
            BabyLoadingTypography.headlineLarge,
            BabyLoadingTypography.headlineMedium,
            BabyLoadingTypography.headlineSmall,
            BabyLoadingTypography.titleLarge,
            BabyLoadingTypography.titleMedium,
            BabyLoadingTypography.titleSmall,
            BabyLoadingTypography.bodyLarge,
            BabyLoadingTypography.bodyMedium,
            BabyLoadingTypography.bodySmall,
            BabyLoadingTypography.labelLarge,
            BabyLoadingTypography.labelMedium,
            BabyLoadingTypography.labelSmall,
        )

        styles.forEach { style ->
            assertSame(BabyLoadingFontFamily, style.fontFamily)
        }
    }
}
