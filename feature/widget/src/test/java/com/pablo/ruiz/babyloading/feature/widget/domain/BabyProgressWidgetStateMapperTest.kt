package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyProgressWidgetStateMapperTest {
    private val currentDate = LocalDate.of(2026, 8, 15)
    private val mapper = BabyProgressWidgetStateMapper(
        CalculatePregnancyProgressUseCase(
            calculator = PregnancyCalculator(),
            clock = Clock.fixed(Instant.parse("2026-08-15T12:00:00Z"), ZoneOffset.UTC),
        ),
    )

    @Test
    fun missingDateProducesSetupStateWithoutDailyRefresh() {
        val state = mapper.map(lastPeriodDate = null)

        assertSame(BabyProgressWidgetState.NeedsSetup, state)
        assertTrue(!state.requiresDailyRefresh)
    }

    @Test
    fun savedOngoingDateProducesCurrentProgressDetails() {
        val state = mapper.map(
            lastPeriodDate = LocalDate.of(2026, 3, 1),
        ) as BabyProgressWidgetState.Ongoing

        assertEquals(23, state.progress.gestationalAge.completedWeeks)
        assertEquals(6, state.progress.gestationalAge.daysIntoWeek)
        assertEquals(DueDateRelation.Upcoming(113), state.progress.dueDateRelation)
        assertTrue(state.requiresDailyRefresh)
    }

    @Test
    fun futureDateProducesInvalidStateWithoutDailyRefresh() {
        val state = mapper.map(lastPeriodDate = currentDate.plusDays(1))

        assertSame(BabyProgressWidgetState.InvalidFutureLastPeriodDate, state)
        assertTrue(!state.requiresDailyRefresh)
    }

    @Test
    fun pregnancyPhaseBoundariesMapThroughFortyTwoWeeks() {
        val fortyWeeksSixDays = mapper.map(
            currentDate.minusDays(286),
        ) as BabyProgressWidgetState.Ongoing
        val fortyOneWeeks = mapper.map(
            currentDate.minusDays(287),
        ) as BabyProgressWidgetState.LateTerm
        val fortyOneWeeksSixDays = mapper.map(
            currentDate.minusDays(293),
        ) as BabyProgressWidgetState.LateTerm
        val fortyTwoWeeks = mapper.map(
            currentDate.minusDays(294),
        ) as BabyProgressWidgetState.PostTerm

        assertEquals(40, fortyWeeksSixDays.progress.gestationalAge.completedWeeks)
        assertEquals(6, fortyWeeksSixDays.progress.gestationalAge.daysIntoWeek)
        assertEquals(DueDateRelation.Elapsed(7), fortyOneWeeks.progress.dueDateRelation)
        assertEquals(41, fortyOneWeeksSixDays.progress.gestationalAge.completedWeeks)
        assertEquals(6, fortyOneWeeksSixDays.progress.gestationalAge.daysIntoWeek)
        assertEquals(DueDateRelation.Elapsed(14), fortyTwoWeeks.progress.dueDateRelation)
        assertTrue(fortyWeeksSixDays.requiresDailyRefresh)
        assertTrue(fortyOneWeeks.requiresDailyRefresh)
        assertTrue(fortyOneWeeksSixDays.requiresDailyRefresh)
        assertTrue(fortyTwoWeeks.requiresDailyRefresh)
    }
}
