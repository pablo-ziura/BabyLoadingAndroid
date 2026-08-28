package com.pablo.ruiz.babyloading.core.pregnancy.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PregnancyCalculatorTest {
    private val calculator = PregnancyCalculator()

    @Test
    fun dueDateIsExactlyTwoHundredEightyDaysAfterLastPeriod() {
        val lastPeriodDate = LocalDate.of(2025, 5, 10)

        assertEquals(
            LocalDate.of(2026, 2, 14),
            calculator.estimatedDueDate(lastPeriodDate),
        )
    }

    @Test
    fun gestationalAgeUsesCompletedWeeksAndRemainingDays() {
        val lastPeriodDate = LocalDate.of(2026, 1, 1)

        val age = calculator.gestationalAge(
            lastPeriodDate = lastPeriodDate,
            currentDate = lastPeriodDate.plusDays(24),
        )

        assertEquals(3, age.completedWeeks)
        assertEquals(3, age.daysIntoWeek)
        assertEquals(24, age.elapsedDays)
    }

    @Test
    fun futureLastPeriodProducesAnInvalidProgressState() {
        val currentDate = LocalDate.of(2026, 1, 1)
        val futureDate = currentDate.plusDays(1)

        assertEquals(
            PregnancyProgress.InvalidFutureLastPeriodDate(futureDate),
            calculator.progress(futureDate, currentDate),
        )
    }

    @Test
    fun phaseBoundariesMatchTheSharedClinicalContract() {
        val lastPeriodDate = LocalDate.of(2026, 1, 1)

        assertEquals(PregnancyPhase.Ongoing, phaseAt(lastPeriodDate, 279))
        assertEquals(PregnancyPhase.Ongoing, phaseAt(lastPeriodDate, 280))
        assertEquals(PregnancyPhase.Ongoing, phaseAt(lastPeriodDate, 286))
        assertEquals(PregnancyPhase.LateTerm, phaseAt(lastPeriodDate, 287))
        assertEquals(PregnancyPhase.LateTerm, phaseAt(lastPeriodDate, 293))
        assertEquals(PregnancyPhase.PostTerm, phaseAt(lastPeriodDate, 294))
    }

    @Test
    fun dueDateRelationPreservesUpcomingTodayAndElapsedDays() {
        val dueDate = LocalDate.of(2026, 10, 8)

        assertEquals(
            DueDateRelation.Upcoming(1),
            calculator.dueDateRelation(dueDate, dueDate.minusDays(1)),
        )
        assertEquals(
            DueDateRelation.Today,
            calculator.dueDateRelation(dueDate, dueDate),
        )
        assertEquals(
            DueDateRelation.Elapsed(1),
            calculator.dueDateRelation(dueDate, dueDate.plusDays(1)),
        )
    }

    private fun phaseAt(lastPeriodDate: LocalDate, elapsedDays: Long): PregnancyPhase {
        val progress = calculator.progress(lastPeriodDate, lastPeriodDate.plusDays(elapsedDays))
        assertTrue(progress is PregnancyProgress.Active)
        return (progress as PregnancyProgress.Active).progress.phase
    }
}
