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
            completedFraction = progress.completedFraction,
            estimatedDueDate = progress.estimatedDueDate,
        )
    }
}
