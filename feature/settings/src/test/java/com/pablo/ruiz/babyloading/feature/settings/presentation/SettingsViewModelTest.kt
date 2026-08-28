package com.pablo.ruiz.babyloading.feature.settings.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.SavePregnancyDateUseCase
import com.pablo.ruiz.babyloading.feature.settings.domain.usecase.ObserveSettingsUseCase
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        changeNotifier = PregnancyDataChangeNotifier { },
    )

    @Test
    fun storedDateLoadsWithEstimatedDueDateAndCanBeSavedAgain() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(LocalDate.of(2026, 5, 10), state.selectedDate)
        assertEquals(LocalDate.of(2027, 2, 14), state.estimatedDueDate)
        assertFalse(state.hasChanges)
        assertTrue(state.canSave)
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

    @Test
    fun storedFutureDateIsPreparedForCorrection() = runTest {
        repository.date.value = LocalDate.of(2026, 8, 16)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasStoredFutureDate)
        assertEquals(LocalDate.of(2026, 8, 15), state.selectedDate)
        assertNull(state.estimatedDueDate)

        viewModel.onEvent(SettingsEvent.DateSelected(LocalDate.of(2026, 8, 14)))
        assertTrue(viewModel.uiState.value.hasStoredFutureDate)
        viewModel.onEvent(SettingsEvent.SaveDate)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasStoredFutureDate)
        assertEquals(LocalDate.of(2026, 8, 14), repository.date.value)
    }

    @Test
    fun languageChangesAreObservedThroughTheSettingsUseCase() = runTest {
        val languageRepository = MutableLanguageRepository(AppLanguage.English)
        val viewModel = createViewModel(languageRepository)
        advanceUntilIdle()

        languageRepository.language = AppLanguage.Spanish
        languageRepository.emit(AppLanguage.Spanish)
        advanceUntilIdle()

        assertEquals(AppLanguage.Spanish, viewModel.uiState.value.appLanguage)
    }

    @Test
    fun concurrentSaveEventsPersistOnlyOnce() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.DateSelected(LocalDate.of(2026, 5, 12)))
        viewModel.onEvent(SettingsEvent.SaveDate)
        viewModel.onEvent(SettingsEvent.SaveDate)
        advanceUntilIdle()

        assertEquals(1, repository.saveCount)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun persistenceFailureRestoresSavingState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.failOnSave = true

        viewModel.onEvent(SettingsEvent.DateSelected(LocalDate.of(2026, 5, 12)))
        viewModel.onEvent(SettingsEvent.SaveDate)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaving)
        assertFalse(viewModel.uiState.value.saveCompleted)
    }

    private fun createViewModel(
        languageRepository: AppLanguageRepository = MutableLanguageRepository(AppLanguage.English),
    ): SettingsViewModel {
        return SettingsViewModel(
            observeSettings = ObserveSettingsUseCase(
                pregnancyRepository = repository,
                calculateProgress = CalculatePregnancyProgressUseCase(calculator, clock),
                languageRepository = languageRepository,
                clock = clock,
            ),
            savePregnancyDate = saveUseCase,
            calculator = calculator,
        )
    }

    private class FakePregnancyRepository(initialDate: LocalDate) : PregnancyRepository {
        val date = MutableStateFlow<LocalDate?>(initialDate)
        var saveCount = 0
        var failOnSave = false
        override val lastPeriodDate: Flow<LocalDate?> = date

        override suspend fun setLastPeriodDate(date: LocalDate) {
            saveCount += 1
            if (failOnSave) error("Persistence failed")
            this.date.value = date
        }

        override suspend fun clearLastPeriodDate() {
            date.value = null
        }
    }

    private class MutableLanguageRepository(
        var language: AppLanguage,
    ) : AppLanguageRepository {
        private val mutableChanges = MutableSharedFlow<AppLanguage>()
        override val changes: Flow<AppLanguage> = mutableChanges

        override fun currentLanguage(): AppLanguage = language

        override suspend fun refreshIfChanged(): Boolean = false

        suspend fun emit(language: AppLanguage) {
            mutableChanges.emit(language)
        }
    }
}
