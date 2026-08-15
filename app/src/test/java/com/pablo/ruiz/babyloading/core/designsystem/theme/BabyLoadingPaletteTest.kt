package com.pablo.ruiz.babyloading.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BabyLoadingPaletteTest {
    @Test
    fun primaryContentContrastMeetsAccessibilityThreshold() {
        val contrast = contrastRatio(
            foreground = BabyLoadingColorScheme.onPrimary,
            background = BabyLoadingColorScheme.primary,
        )

        assertTrue("Expected contrast >= 4.5, was $contrast", contrast >= 4.5)
    }

    @Test
    fun surfaceContentContrastMeetsAccessibilityThreshold() {
        val contrast = contrastRatio(
            foreground = BabyLoadingColorScheme.onSurface,
            background = BabyLoadingColorScheme.surface,
        )

        assertTrue("Expected contrast >= 4.5, was $contrast", contrast >= 4.5)
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        return (max(foregroundLuminance, backgroundLuminance) + 0.05) /
            (min(foregroundLuminance, backgroundLuminance) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(component: Float): Double {
            val value = component.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }

        return 0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)
    }
}
