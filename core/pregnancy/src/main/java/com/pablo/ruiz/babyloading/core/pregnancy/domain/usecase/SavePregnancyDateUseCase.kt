package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class SavePregnancyDateUseCase @Inject constructor(
    private val repository: PregnancyRepository,
    private val validator: PregnancyDateValidator,
    private val clock: Clock,
    private val changeNotifier: PregnancyDataChangeNotifier,
) {
    suspend operator fun invoke(lastPeriodDate: LocalDate): PregnancyDateValidation {
        val validation = validator.validate(
            lastPeriodDate = lastPeriodDate,
            currentDate = LocalDate.now(clock),
        )
        if (validation == PregnancyDateValidation.Valid) {
            repository.setLastPeriodDate(lastPeriodDate)
            runCatching { changeNotifier.onPregnancyDataChanged() }
        }
        return validation
    }
}
