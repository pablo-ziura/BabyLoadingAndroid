package com.pablo.ruiz.babyloading.app.bootstrap

import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.ObservePregnancySetupUseCase
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppBootstrapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun bootstrapTracksStoredDatePresenceIncludingHistoricalDates() = runTest {
        val repository = FakePregnancyRepository()
        val viewModel = AppBootstrapViewModel(ObservePregnancySetupUseCase(repository))

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isPregnancyConfigured)

        repository.date.value = LocalDate.of(2020, 1, 1)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPregnancyConfigured)
    }

    private class FakePregnancyRepository : PregnancyRepository {
        val date = MutableStateFlow<LocalDate?>(null)
        override val lastPeriodDate: Flow<LocalDate?> = date

        override suspend fun setLastPeriodDate(date: LocalDate) {
            this.date.value = date
        }

        override suspend fun clearLastPeriodDate() {
            date.value = null
        }
    }
}
