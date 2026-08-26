package com.pablo.ruiz.babyloading.feature.widget.data

import java.time.Clock
import java.time.Instant
import java.time.LocalDate

internal object WidgetDailyRefreshSchedule {
    fun nextRefreshAt(clock: Clock): Instant {
        return LocalDate.now(clock)
            .plusDays(1)
            .atStartOfDay(clock.zone)
            .toInstant()
    }
}
