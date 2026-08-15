package com.pablo.ruiz.babyloading.feature.settings.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )
    private val calculator = PregnancyCalculator()
    private val repository = FakePregnancyRepository(LocalDate.of(2026, 5, 10))
    private val saveUseCase = SavePregnancyDateUseCase(
        repository = repository,
        validator = PregnancyDateValidator(),
        clock = clock,
    )

    @Test
    fun storedDateLoadsWithEstimatedDueDateAndNoPendingChanges() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(LocalDate.of(2026, 5, 10), state.selectedDate)
        assertEquals(LocalDate.of(2027, 2, 14), state.estimatedDueDate)
        assertFalse(state.hasChanges)
        assertFalse(state.canSave)
    }

    @Test
    fun selectingAndSavingAValidDateUpdatesRepository() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val updatedDate = LocalDate.of(2026, 5, 12)

        viewModel.onEvent(SettingsEvent.DateSelected(updatedDate))
        assertTrue(viewModel.uiState.value.canSave)
        assertEquals(LocalDate.of(2027, 2, 16), viewModel.uiState.value.estimatedDueDate)

        viewModel.onEvent(SettingsEvent.SaveDate)
        advanceUntilIdle()

        assertEquals(updatedDate, repository.date.value)
        assertEquals(updatedDate, viewModel.uiState.value.savedDate)
        assertTrue(viewModel.uiState.value.saveCompleted)
        assertFalse(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun invalidDirectSelectionIsNotPersisted() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.DateSelected(LocalDate.of(2026, 8, 16)))
        viewModel.onEvent(SettingsEvent.SaveDate)
        advanceUntilIdle()

        assertEquals(LocalDate.of(2026, 5, 10), repository.date.value)
        assertEquals(SettingsValidationError.FutureDate, viewModel.uiState.value.validationError)
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            repository = repository,
            savePregnancyDate = saveUseCase,
            calculator = calculator,
            clock = clock,
        )
    }

    private class FakePregnancyRepository(initialDate: LocalDate) : PregnancyRepository {
        val date = MutableStateFlow<LocalDate?>(initialDate)
        override val lastPeriodDate: Flow<LocalDate?> = date

        override suspend fun setLastPeriodDate(date: LocalDate) {
            this.date.value = date
        }

        override suspend fun clearLastPeriodDate() {
            date.value = null
        }
    }
}
