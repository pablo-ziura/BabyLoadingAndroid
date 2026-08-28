package com.pablo.ruiz.babyloading.core.pregnancy.domain.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface PregnancyRepository {
    val lastPeriodDate: Flow<LocalDate?>

    suspend fun setLastPeriodDate(date: LocalDate)

    suspend fun clearLastPeriodDate()
}
