package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.ActivePregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import java.time.LocalDate

class BabyProgressWidgetStateFactory(
    private val calculator: PregnancyCalculator,
) {
    fun create(
        lastPeriodDate: LocalDate?,
        currentDate: LocalDate,
    ): BabyProgressWidgetState {
        if (lastPeriodDate == null) return BabyProgressWidgetState.NeedsSetup
        return when (val progress = calculator.progress(lastPeriodDate, currentDate)) {
            is PregnancyProgress.InvalidFutureLastPeriodDate -> {
                BabyProgressWidgetState.InvalidFutureLastPeriodDate
            }

            is PregnancyProgress.Active -> stateFor(progress.progress)
        }
    }

    private fun stateFor(progress: ActivePregnancyProgress): BabyProgressWidgetState {
        val details = BabyProgressWidgetDetails(
            gestationalAge = progress.gestationalAge,
            dueDateRelation = progress.dueDateRelation,
        )
        return when (progress.phase) {
            PregnancyPhase.Ongoing -> BabyProgressWidgetState.Ongoing(details)
            PregnancyPhase.LateTerm -> BabyProgressWidgetState.LateTerm(details)
            PregnancyPhase.PostTerm -> BabyProgressWidgetState.PostTerm(details)
        }
    }
}
