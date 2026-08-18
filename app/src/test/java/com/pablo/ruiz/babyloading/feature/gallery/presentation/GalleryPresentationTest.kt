package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryPresentationTest {
    @Test
    fun bitmapSamplingUsesPowersOfTwoWithoutUndershootingTarget() {
        assertEquals(1, GalleryBitmapLoader.calculateInSampleSize(400, 300, 512))
        assertEquals(2, GalleryBitmapLoader.calculateInSampleSize(4000, 3000, 1000))
        assertEquals(4, GalleryBitmapLoader.calculateInSampleSize(8000, 6000, 1000))
    }

    @Test
    fun captureDateUsesRequestedLocaleAndZone() {
        val instant = Instant.parse("2026-08-15T23:30:00Z")

        assertEquals(
            "Aug 16, 2026",
            GalleryDateFormatter.format(instant, Locale.US, ZoneOffset.ofHours(2)),
        )
    }

    @Test
    fun localDateUsesRequestedLocale() {
        assertEquals(
            "Aug 25, 2026",
            GalleryDateFormatter.format(LocalDate.parse("2026-08-25"), Locale.US),
        )
    }

    @Test
    fun unsupportedCadenceFallsBackToWeekly() {
        assertEquals(TrackingCadence.Weekly, TrackingCadence.fromIntervalDays(8))
    }
}
