package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.SavePregnancyDateUseCase
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )
    private val repository = FakePregnancyRepository()
    private val saveUseCase = SavePregnancyDateUseCase(
        repository = repository,
        validator = PregnancyDateValidator(),
        clock = clock,
        changeNotifier = PregnancyDataChangeNotifier { },
    )

    @Test
    fun missingStoredDateRequiresExplicitSelection() = runTest {
        val viewModel = OnboardingViewModel(repository, saveUseCase, clock)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canContinue)
    }

    @Test
    fun continuePersistsSelectedDate() = runTest {
        val viewModel = OnboardingViewModel(repository, saveUseCase, clock)
        val selectedDate = LocalDate.of(2026, 5, 10)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.DateSelected(selectedDate))
        viewModel.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        assertEquals(selectedDate, repository.storedDate.value)
        assertEquals(selectedDate, viewModel.uiState.value.storedDate)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun futureDateShowsValidationErrorWithoutPersisting() = runTest {
        val viewModel = OnboardingViewModel(repository, saveUseCase, clock)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.DateSelected(LocalDate.of(2026, 8, 16)))
        viewModel.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        assertNull(repository.storedDate.value)
        assertEquals(
            OnboardingValidationError.FutureDate,
            viewModel.uiState.value.validationError,
        )
    }

    @Test
    fun dateOlderThanFortyTwoWeeksShowsValidationErrorWithoutPersisting() = runTest {
        val viewModel = OnboardingViewModel(repository, saveUseCase, clock)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.DateSelected(LocalDate.of(2025, 10, 24)))
        viewModel.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        assertNull(repository.storedDate.value)
        assertEquals(
            OnboardingValidationError.DateTooOld,
            viewModel.uiState.value.validationError,
        )
    }

    private class FakePregnancyRepository : PregnancyRepository {
        val storedDate = MutableStateFlow<LocalDate?>(null)
        override val lastPeriodDate: Flow<LocalDate?> = storedDate

        override suspend fun setLastPeriodDate(date: LocalDate) {
            storedDate.value = date
        }

        override suspend fun clearLastPeriodDate() {
            storedDate.value = null
        }
    }
}
