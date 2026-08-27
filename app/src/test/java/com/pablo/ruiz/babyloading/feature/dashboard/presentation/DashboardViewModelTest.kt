package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageChanges
import com.pablo.ruiz.babyloading.core.localization.AppLanguageProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val currentDate = LocalDate.of(2026, 8, 15)
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )
    private val pregnancyRepository = FakePregnancyRepository()
    private val contentRepository = FakeContentRepository()

    @Test
    fun activePregnancyLoadsProgressAndLocalizedContent() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(20)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        val progress = state.progress as PregnancyProgress.Active
        assertEquals(20, progress.progress.gestationalAge.completedWeeks)
        assertEquals(PregnancyPhase.Ongoing, progress.progress.phase)
        assertEquals(20, state.weekContent?.week)
        assertEquals(AppLanguage.Spanish, contentRepository.requestedLanguages.last())
    }

    @Test
    fun foregroundLanguageChangeReloadsWeeklyContent() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(20)
        val languageProvider = MutableLanguageProvider(AppLanguage.English)
        val languageChanges = MutableLanguageChanges()
        val viewModel = createViewModel(languageProvider, languageChanges)

        advanceUntilIdle()
        assertEquals("a lentil", viewModel.uiState.value.weekContent?.babySizeLabel)

        languageProvider.language = AppLanguage.Spanish
        languageChanges.emit(AppLanguage.Spanish)
        advanceUntilIdle()

        assertEquals("una lenteja", viewModel.uiState.value.weekContent?.babySizeLabel)
        assertEquals(
            listOf(AppLanguage.English, AppLanguage.Spanish),
            contentRepository.requestedLanguages,
        )
    }

    @Test
    fun ongoingPregnancyBeforeWeekSixHasNoWeeklyEditorialEntry() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(5)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val progress = viewModel.uiState.value.progress as PregnancyProgress.Active
        assertEquals(PregnancyPhase.Ongoing, progress.progress.phase)
        assertNull(viewModel.uiState.value.weekContent)
    }

    @Test
    fun lateAndPostTermProgressDoNotLoadEditorialContent() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(41)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val lateTerm = viewModel.uiState.value.progress as PregnancyProgress.Active
        assertEquals(PregnancyPhase.LateTerm, lateTerm.progress.phase)
        assertNull(viewModel.uiState.value.weekContent)

        pregnancyRepository.date.value = currentDate.minusWeeks(42)
        advanceUntilIdle()
        val postTerm = viewModel.uiState.value.progress as PregnancyProgress.Active
        assertEquals(PregnancyPhase.PostTerm, postTerm.progress.phase)
        assertNull(viewModel.uiState.value.weekContent)
        assertEquals(emptyList<AppLanguage>(), contentRepository.requestedLanguages)
    }

    @Test
    fun historicalFutureDateProducesAnInvalidProgressWithoutEditorialContent() = runTest {
        pregnancyRepository.date.value = currentDate.plusDays(1)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(
            PregnancyProgress.InvalidFutureLastPeriodDate(currentDate.plusDays(1)),
            viewModel.uiState.value.progress,
        )
        assertNull(viewModel.uiState.value.weekContent)
    }

    private fun createViewModel(
        languageProvider: AppLanguageProvider = MutableLanguageProvider(AppLanguage.Spanish),
        languageChanges: AppLanguageChanges = MutableLanguageChanges(),
    ): DashboardViewModel {
        return DashboardViewModel(
            pregnancyRepository = pregnancyRepository,
            contentRepository = contentRepository,
            calculateProgress = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock),
            languageProvider = languageProvider,
            languageChanges = languageChanges,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        )
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

    private class FakeContentRepository : PregnancyContentRepository {
        val requestedLanguages = mutableListOf<AppLanguage>()

        override fun contentForWeek(week: Int, language: AppLanguage): WeekContent? {
            requestedLanguages += language
            if (week !in 6..40) return null
            val contentWeek = week
            return WeekContent(
                week = contentWeek,
                babySize = BabySize.Lentil,
                babySizeLabel = when (language) {
                    AppLanguage.English -> "a lentil"
                    AppLanguage.Spanish -> "una lenteja"
                },
                milestoneTitle = "Semana $contentWeek",
                keyEvents = listOf("Evento"),
            )
        }

        override fun allContent(language: AppLanguage): List<WeekContent> = emptyList()
    }

    private class MutableLanguageProvider(
        var language: AppLanguage,
    ) : AppLanguageProvider {
        override fun currentLanguage(): AppLanguage = language
    }

    private class MutableLanguageChanges : AppLanguageChanges {
        private val mutableChanges = MutableSharedFlow<AppLanguage>()
        override val changes: Flow<AppLanguage> = mutableChanges

        override suspend fun refreshIfLanguageChanged(): Boolean = false

        suspend fun emit(language: AppLanguage) {
            mutableChanges.emit(language)
        }
    }
}
