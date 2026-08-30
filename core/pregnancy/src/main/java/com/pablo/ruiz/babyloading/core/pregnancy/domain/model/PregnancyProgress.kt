package com.pablo.ruiz.babyloading.core.pregnancy.domain.model

import java.time.LocalDate

sealed interface PregnancyProgress {
    data class Active(
        val progress: ActivePregnancyProgress,
    ) : PregnancyProgress

    data class InvalidFutureLastPeriodDate(
        val lastPeriodDate: LocalDate,
    ) : PregnancyProgress
}

data class ActivePregnancyProgress(
    val lastPeriodDate: LocalDate,
    val estimatedDueDate: LocalDate,
    val gestationalAge: GestationalAge,
    val phase: PregnancyPhase,
    val dueDateRelation: DueDateRelation,
)
