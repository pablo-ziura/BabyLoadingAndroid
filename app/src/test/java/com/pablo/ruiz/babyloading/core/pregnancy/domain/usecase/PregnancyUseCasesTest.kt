package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PregnancyUseCasesTest {
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )
    private val repository = FakePregnancyRepository()

    @Test
    fun calculateProgressUsesInjectedClock() {
        val useCase = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock)

        val progress = useCase(LocalDate.of(2026, 8, 1))

        assertEquals(2, progress.gestationalAge.completedWeeks)
        assertEquals(0, progress.gestationalAge.daysIntoWeek)
    }

    @Test
    fun saveDatePersistsOnlyValidValues() = runTest {
        val useCase = SavePregnancyDateUseCase(repository, PregnancyDateValidator(), clock)
        val validDate = LocalDate.of(2026, 8, 1)

        assertEquals(PregnancyDateValidation.Valid, useCase(validDate))
        assertEquals(validDate, repository.savedDate.value)

        assertEquals(PregnancyDateValidation.FutureDate, useCase(LocalDate.of(2026, 8, 16)))
        assertEquals(validDate, repository.savedDate.value)

        assertEquals(PregnancyDateValidation.DateTooOld, useCase(LocalDate.of(2025, 10, 24)))
        assertEquals(validDate, repository.savedDate.value)
    }

    @Test
    fun clearDateRemovesStoredValue() = runTest {
        repository.setLastPeriodDate(LocalDate.of(2026, 8, 1))

        repository.clearLastPeriodDate()

        assertNull(repository.savedDate.value)
    }

    private class FakePregnancyRepository : PregnancyRepository {
        val savedDate = MutableStateFlow<LocalDate?>(null)
        override val lastPeriodDate: Flow<LocalDate?> = savedDate

        override suspend fun setLastPeriodDate(date: LocalDate) {
            savedDate.value = date
        }

        override suspend fun clearLastPeriodDate() {
            savedDate.value = null
        }
    }
}
