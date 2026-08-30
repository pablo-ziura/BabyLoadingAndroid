package com.pablo.ruiz.babyloading.feature.widget.data

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetDetails
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWidgetRefreshRepositoryTest {
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T21:30:00Z"),
        ZoneId.of("Europe/Madrid"),
    )
    private val alarmDataSource = RecordingWidgetRefreshAlarmDataSource()
    private val repository = DefaultWidgetRefreshRepository(alarmDataSource, clock)

    @Test
    fun activeStateSchedulesTheNextLocalMidnight() {
        repository.synchronize(ongoingState())

        assertEquals(listOf(Instant.parse("2026-08-15T22:00:00Z")), alarmDataSource.scheduled)
        assertEquals(0, alarmDataSource.cancelCount)
    }

    @Test
    fun setupAndInvalidStatesCancelDailyRefresh() {
        repository.synchronize(BabyProgressWidgetState.NeedsSetup)
        repository.synchronize(BabyProgressWidgetState.InvalidFutureLastPeriodDate)

        assertTrue(alarmDataSource.scheduled.isEmpty())
        assertEquals(2, alarmDataSource.cancelCount)
    }

    @Test
    fun explicitCancellationDelegatesToAlarmDataSource() {
        repository.cancel()

        assertEquals(1, alarmDataSource.cancelCount)
    }

    private fun ongoingState() = BabyProgressWidgetState.Ongoing(
        BabyProgressWidgetDetails(
            gestationalAge = GestationalAge(24, 0, 168),
            dueDateRelation = DueDateRelation.Upcoming(112),
        ),
    )
}

private class RecordingWidgetRefreshAlarmDataSource : WidgetRefreshAlarmDataSource {
    val scheduled = mutableListOf<Instant>()
    var cancelCount = 0

    override fun schedule(refreshAt: Instant) {
        scheduled += refreshAt
    }

    override fun cancel() {
        cancelCount += 1
    }
}
