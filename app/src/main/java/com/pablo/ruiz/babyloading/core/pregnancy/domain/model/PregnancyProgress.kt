package com.pablo.ruiz.babyloading.core.pregnancy.domain.model

import java.time.LocalDate

data class PregnancyProgress(
    val lastPeriodDate: LocalDate,
    val estimatedDueDate: LocalDate,
    val gestationalAge: GestationalAge,
    val daysRemaining: Int,
    val completedFraction: Float,
    val stage: PregnancyStage,
) {
    init {
        require(daysRemaining >= 0) { "Remaining days cannot be negative" }
        require(completedFraction in 0f..1f) { "Completed fraction must be between zero and one" }
    }
}
