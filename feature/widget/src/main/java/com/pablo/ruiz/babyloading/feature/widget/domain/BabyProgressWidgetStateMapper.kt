package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.ActivePregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import java.time.LocalDate
import javax.inject.Inject

class BabyProgressWidgetStateMapper @Inject constructor(
    private val calculateProgress: CalculatePregnancyProgressUseCase,
) {
    fun map(lastPeriodDate: LocalDate?): BabyProgressWidgetState {
        if (lastPeriodDate == null) return BabyProgressWidgetState.NeedsSetup
        return when (val progress = calculateProgress(lastPeriodDate)) {
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
