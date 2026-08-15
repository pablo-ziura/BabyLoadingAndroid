package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class CalculatePregnancyProgressUseCase @Inject constructor(
    private val calculator: PregnancyCalculator,
    private val clock: Clock,
) {
    operator fun invoke(lastPeriodDate: LocalDate): PregnancyProgress {
        return calculator.progress(
            lastPeriodDate = lastPeriodDate,
            currentDate = LocalDate.now(clock),
        )
    }
}
