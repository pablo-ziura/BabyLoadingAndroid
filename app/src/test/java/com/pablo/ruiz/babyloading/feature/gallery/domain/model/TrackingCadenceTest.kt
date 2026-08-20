package com.pablo.ruiz.babyloading.feature.gallery.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingCadenceTest {
    @Test
    fun noCaptureNeedsAnInitialCapture() {
        assertEquals(
            TrackingStatus.NeedsInitialCapture,
            TrackingCadence.Weekly.trackingStatus(
                lastCapture = null,
                asOf = instant("2026-08-20"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }

    @Test
    fun captureAtCadenceLimitIsStillUpToDate() {
        assertEquals(
            TrackingStatus.UpToDate(LocalDate.parse("2026-08-20")),
            TrackingCadence.Weekly.trackingStatus(
                lastCapture = instant("2026-08-13"),
                asOf = instant("2026-08-20"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }

    @Test
    fun capturePastCadenceIsPending() {
        assertEquals(
            TrackingStatus.Pending(LocalDate.parse("2026-08-19")),
            TrackingCadence.Weekly.trackingStatus(
                lastCapture = instant("2026-08-12"),
                asOf = instant("2026-08-20"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }

    @Test
    fun everyFourWeeksKeepsCaptureUpToDate() {
        assertEquals(
            TrackingStatus.UpToDate(LocalDate.parse("2026-09-07")),
            TrackingCadence.EveryFourWeeks.trackingStatus(
                lastCapture = instant("2026-08-10"),
                asOf = instant("2026-08-20"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }

    @Test
    fun futureCaptureDoesNotCreateNegativeElapsedDays() {
        assertEquals(
            TrackingStatus.UpToDate(LocalDate.parse("2026-08-27")),
            TrackingCadence.Weekly.trackingStatus(
                lastCapture = instant("2026-08-20"),
                asOf = instant("2026-08-19"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }

    private fun instant(date: String): Instant = Instant.parse("${date}T12:00:00Z")
}
