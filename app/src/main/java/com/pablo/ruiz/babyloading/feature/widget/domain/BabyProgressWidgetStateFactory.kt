package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import java.time.LocalDate

class BabyProgressWidgetStateFactory(
    private val calculator: PregnancyCalculator,
) {
    fun create(
        lastPeriodDate: LocalDate?,
        currentDate: LocalDate,
    ): BabyProgressWidgetState {
        if (lastPeriodDate == null) return BabyProgressWidgetState.NeedsSetup
        val progress = calculator.progress(lastPeriodDate, currentDate)
        return BabyProgressWidgetState.Progress(
            completedWeeks = progress.gestationalAge.completedWeeks,
            daysIntoWeek = progress.gestationalAge.daysIntoWeek,
            daysRemaining = progress.daysRemaining,
            completedFraction = (
                progress.gestationalAge.completedWeeks.toFloat() / TOTAL_PREGNANCY_WEEKS
                ).coerceIn(0f, 1f),
            estimatedDueDate = progress.estimatedDueDate,
        )
    }

    private companion object {
        const val TOTAL_PREGNANCY_WEEKS = 40f
    }
}
