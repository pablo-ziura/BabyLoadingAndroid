package com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        val progress = useCase(LocalDate.of(2026, 8, 1)) as PregnancyProgress.Active

        assertEquals(2, progress.progress.gestationalAge.completedWeeks)
        assertEquals(0, progress.progress.gestationalAge.daysIntoWeek)
    }

    @Test
    fun saveDatePersistsOnlyValidValues() = runTest {
        val notifier = RecordingChangeNotifier(repository)
        val useCase = SavePregnancyDateUseCase(
            repository,
            PregnancyDateValidator(),
            clock,
            notifier,
        )
        val validDate = LocalDate.of(2026, 8, 1)

        assertEquals(PregnancyDateValidation.Valid, useCase(validDate))
        assertEquals(validDate, repository.savedDate.value)

        assertEquals(PregnancyDateValidation.FutureDate, useCase(LocalDate.of(2026, 8, 16)))
        assertEquals(validDate, repository.savedDate.value)

        assertEquals(PregnancyDateValidation.DateTooOld, useCase(LocalDate.of(2025, 10, 24)))
        assertEquals(validDate, repository.savedDate.value)
        assertEquals(1, notifier.changeCount)
        assertTrue(notifier.observedCommittedDate)
    }

    @Test
    fun widgetNotificationFailureDoesNotLoseSavedDate() = runTest {
        val useCase = SavePregnancyDateUseCase(
            repository = repository,
            validator = PregnancyDateValidator(),
            clock = clock,
            changeNotifier = PregnancyDataChangeNotifier {
                throw IOException("Widget host unavailable")
            },
        )
        val date = LocalDate.of(2026, 8, 1)

        assertEquals(PregnancyDateValidation.Valid, useCase(date))

        assertEquals(date, repository.savedDate.value)
    }

    @Test
    fun setupObservationUsesStoredDatePresenceIncludingHistoricalValues() = runTest {
        val useCase = ObservePregnancySetupUseCase(repository)

        assertNull(useCase().first())
        val historicalDate = LocalDate.of(2020, 1, 1)
        repository.setLastPeriodDate(historicalDate)
        assertEquals(historicalDate, useCase().first())
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

    private class RecordingChangeNotifier(
        private val repository: FakePregnancyRepository,
    ) : PregnancyDataChangeNotifier {
        var changeCount = 0
        var observedCommittedDate = false

        override suspend fun onPregnancyDataChanged() {
            changeCount += 1
            observedCommittedDate = repository.savedDate.value != null
        }
    }
}
