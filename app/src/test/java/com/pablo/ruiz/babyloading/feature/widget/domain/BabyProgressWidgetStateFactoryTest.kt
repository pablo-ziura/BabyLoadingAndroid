package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BabyProgressWidgetStateFactoryTest {
    private val factory = BabyProgressWidgetStateFactory(PregnancyCalculator())

    @Test
    fun missingDateProducesSetupState() {
        val state = factory.create(
            lastPeriodDate = null,
            currentDate = LocalDate.of(2026, 8, 15),
        )

        assertSame(BabyProgressWidgetState.NeedsSetup, state)
    }

    @Test
    fun savedDateProducesCurrentProgressSnapshot() {
        val state = factory.create(
            lastPeriodDate = LocalDate.of(2026, 3, 1),
            currentDate = LocalDate.of(2026, 8, 15),
        ) as BabyProgressWidgetState.Progress

        assertEquals(23, state.completedWeeks)
        assertEquals(6, state.daysIntoWeek)
        assertEquals(113, state.daysRemaining)
        assertEquals(23f / 40f, state.completedFraction)
        assertEquals(LocalDate.of(2026, 12, 6), state.estimatedDueDate)
    }

    @Test
    fun elapsedPregnancyIsClampedForWidgetDisplay() {
        val state = factory.create(
            lastPeriodDate = LocalDate.of(2025, 9, 1),
            currentDate = LocalDate.of(2026, 8, 15),
        ) as BabyProgressWidgetState.Progress

        assertEquals(0, state.daysRemaining)
        assertEquals(1f, state.completedFraction)
    }
}
