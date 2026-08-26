package com.pablo.ruiz.babyloading.feature.widget.data

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetDailyRefreshScheduleTest {
    @Test
    fun nextRefreshOccursAtTheNextLocalMidnight() {
        val clock = Clock.fixed(
            Instant.parse("2026-08-15T21:30:00Z"),
            ZoneId.of("Europe/Madrid"),
        )

        val refreshAt = WidgetDailyRefreshSchedule.nextRefreshAt(clock)

        assertEquals(Instant.parse("2026-08-15T22:00:00Z"), refreshAt)
    }

    @Test
    fun nextRefreshUsesTheAdjustedOffsetAfterDaylightSavingTimeChanges() {
        val clock = Clock.fixed(
            Instant.parse("2026-10-24T22:30:00Z"),
            ZoneId.of("Europe/Madrid"),
        )

        val refreshAt = WidgetDailyRefreshSchedule.nextRefreshAt(clock)

        assertEquals(Instant.parse("2026-10-25T23:00:00Z"), refreshAt)
    }
}
