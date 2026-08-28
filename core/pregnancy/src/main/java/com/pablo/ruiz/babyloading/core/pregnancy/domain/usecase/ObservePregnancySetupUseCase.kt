package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePregnancySetupUseCase @Inject constructor(
    private val repository: PregnancyRepository,
) {
    operator fun invoke(): Flow<LocalDate?> = repository.lastPeriodDate
}
