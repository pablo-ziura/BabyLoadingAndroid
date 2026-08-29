package com.pablo.ruiz.babyloading.feature.tracking.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TrackingGuideGeometryTest {
    @Test
    fun guideUsesCenteredNineBySixteenViewport() {
        val guide = calculateTrackingGuideGeometry(width = 1000f, height = 2000f)

        assertEquals(1000f, guide.width, 0.01f)
        assertEquals(1777.78f, guide.height, 0.01f)
        assertEquals(0f, guide.left, 0.01f)
        assertEquals(111.11f, guide.top, 0.01f)
    }

    @Test
    fun nonPositiveViewportIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            calculateTrackingGuideGeometry(width = 0f, height = 100f)
        }
    }
}
