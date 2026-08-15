package com.pablo.ruiz.babyloading.core.pregnancy.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PregnancyCalculatorTest {
    private val calculator = PregnancyCalculator()

    @Test
    fun dueDateIsExactlyTwoHundredEightyDaysAfterLastPeriod() {
        val lastPeriodDate = LocalDate.of(2025, 5, 10)

        val dueDate = calculator.estimatedDueDate(lastPeriodDate)

        assertEquals(LocalDate.of(2026, 2, 14), dueDate)
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
    fun futureLastPeriodIsClampedToWeekZero() {
        val currentDate = LocalDate.of(2026, 1, 1)

        val age = calculator.gestationalAge(
            lastPeriodDate = currentDate.plusDays(1),
            currentDate = currentDate,
        )

        assertEquals(0, age.completedWeeks)
        assertEquals(0, age.daysIntoWeek)
        assertEquals(0, age.elapsedDays)
    }

    @Test
    fun stageBoundariesMatchProductRules() {
        assertEquals(PregnancyStage.Early, calculator.stageFor(0))
        assertEquals(PregnancyStage.Early, calculator.stageFor(5))
        assertEquals(PregnancyStage.Active, calculator.stageFor(6))
        assertEquals(PregnancyStage.Active, calculator.stageFor(40))
        assertEquals(PregnancyStage.PostTerm, calculator.stageFor(41))
        assertEquals(PregnancyStage.PostTerm, calculator.stageFor(42))
        assertEquals(PregnancyStage.NeedsReview, calculator.stageFor(43))
    }

    @Test
    fun completedProgressAndRemainingDaysAreClamped() {
        val lastPeriodDate = LocalDate.of(2025, 1, 1)

        val progress = calculator.progress(
            lastPeriodDate = lastPeriodDate,
            currentDate = lastPeriodDate.plusDays(301),
        )

        assertEquals(0, progress.daysRemaining)
        assertEquals(1f, progress.completedFraction)
        assertTrue(progress.stage == PregnancyStage.NeedsReview)
    }
}
