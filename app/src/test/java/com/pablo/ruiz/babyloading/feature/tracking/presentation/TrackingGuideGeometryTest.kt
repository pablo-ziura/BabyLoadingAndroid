package com.pablo.ruiz.babyloading.feature.tracking.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TrackingGuideGeometryTest {
    @Test
    fun guideIsCenteredWithStableViewportRatios() {
        val guide = calculateTrackingGuideGeometry(width = 1000f, height = 2000f)

        assertEquals(680f, guide.width, 0.01f)
        assertEquals(1320f, guide.height, 0.01f)
        assertEquals(160f, guide.left, 0.01f)
        assertEquals(320f, guide.top, 0.01f)
    }

    @Test
    fun nonPositiveViewportIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            calculateTrackingGuideGeometry(width = 0f, height = 100f)
        }
    }
}
