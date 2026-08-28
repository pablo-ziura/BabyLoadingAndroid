package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservePregnancySetupUseCase @Inject constructor(
    private val repository: PregnancyRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.lastPeriodDate.map { date -> date != null }
}
