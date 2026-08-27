package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyProgressWidgetStateFactoryTest {
    private val factory = BabyProgressWidgetStateFactory(PregnancyCalculator())

    @Test
    fun missingDateProducesSetupStateWithoutDailyRefresh() {
        val state = factory.create(
            lastPeriodDate = null,
            currentDate = LocalDate.of(2026, 8, 15),
        )

        assertSame(BabyProgressWidgetState.NeedsSetup, state)
        assertTrue(!state.requiresDailyRefresh)
    }

    @Test
    fun savedOngoingDateProducesCurrentProgressDetails() {
        val state = factory.create(
            lastPeriodDate = LocalDate.of(2026, 3, 1),
            currentDate = LocalDate.of(2026, 8, 15),
        ) as BabyProgressWidgetState.Ongoing

        assertEquals(23, state.progress.gestationalAge.completedWeeks)
        assertEquals(6, state.progress.gestationalAge.daysIntoWeek)
        assertEquals(DueDateRelation.Upcoming(113), state.progress.dueDateRelation)
        assertTrue(state.requiresDailyRefresh)
    }

    @Test
    fun futureDateProducesInvalidStateWithoutDailyRefresh() {
        val state = factory.create(
            lastPeriodDate = LocalDate.of(2026, 8, 16),
            currentDate = LocalDate.of(2026, 8, 15),
        )

        assertSame(BabyProgressWidgetState.InvalidFutureLastPeriodDate, state)
        assertTrue(!state.requiresDailyRefresh)
    }

    @Test
    fun lateAndPostTermDatesProduceDedicatedRefreshingStates() {
        val lastPeriodDate = LocalDate.of(2026, 1, 1)

        val lateTerm = factory.create(
            lastPeriodDate = lastPeriodDate,
            currentDate = lastPeriodDate.plusDays(287),
        ) as BabyProgressWidgetState.LateTerm
        val postTerm = factory.create(
            lastPeriodDate = lastPeriodDate,
            currentDate = lastPeriodDate.plusDays(294),
        ) as BabyProgressWidgetState.PostTerm

        assertEquals(DueDateRelation.Elapsed(7), lateTerm.progress.dueDateRelation)
        assertEquals(DueDateRelation.Elapsed(14), postTerm.progress.dueDateRelation)
        assertTrue(lateTerm.requiresDailyRefresh)
        assertTrue(postTerm.requiresDailyRefresh)
    }
}
