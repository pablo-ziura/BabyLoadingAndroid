package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.journey.domain.usecase.ObserveJourneyUseCase
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
class JourneyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val currentDate = LocalDate.of(2026, 8, 15)
    private val pregnancyRepository = FakePregnancyRepository()
    private val contentRepository = FakeContentRepository()
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )

    @Test
    fun timelineShowsEditorialWeeksSixThroughFortyWithStatuses() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(20).minusDays(3)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(true, state.isConfigured)
        assertEquals((6..40).toList(), state.weeks.map(JourneyWeekUiModel::week))
        assertEquals(3, state.currentDay)
        assertEquals(JourneyWeekStatus.Completed, state.weeks.first { it.week == 19 }.status)
        assertEquals(JourneyWeekStatus.Current, state.weeks.first { it.week == 20 }.status)
        assertEquals(JourneyWeekStatus.Upcoming, state.weeks.first { it.week == 21 }.status)
    }

    @Test
    fun timelineUsesOnlyWeeklyEditorialContent() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(20)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val weeks = viewModel.uiState.value.weeks
        assertEquals((6..40).toList(), weeks.map(JourneyWeekUiModel::week))
        assertEquals(6, weeks.first().content.week)
        assertEquals(40, weeks.last().content.week)
    }

    @Test
    fun lateAndPostTermProgressClearTheCurrentTimelineMarker() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(41)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.currentWeek)
        assertEquals(0, state.currentDay)
        assertEquals(0, state.weeks.count { it.status == JourneyWeekStatus.Current })

        pregnancyRepository.date.value = currentDate.minusWeeks(42)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.currentWeek)
        assertEquals(0, viewModel.uiState.value.currentDay)
    }

    @Test
    fun futureStoredDateKeepsTheTimelineConfiguredWithoutCurrentMarkers() = runTest {
        pregnancyRepository.date.value = currentDate.plusDays(1)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.isConfigured)
        assertEquals(null, state.currentWeek)
        assertEquals(0, state.currentDay)
    }

    @Test
    fun languageAndForegroundSignalsReloadTheTimeline() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(20)
        val languageRepository = MutableLanguageRepository(AppLanguage.English)
        val viewModel = createViewModel(languageRepository)
        advanceUntilIdle()

        languageRepository.language = AppLanguage.Spanish
        languageRepository.emit(AppLanguage.Spanish)
        advanceUntilIdle()
        viewModel.onEvent(JourneyEvent.Refresh)
        advanceUntilIdle()

        assertEquals(
            listOf(AppLanguage.English, AppLanguage.Spanish, AppLanguage.Spanish),
            contentRepository.requestedLanguages,
        )
    }

    private fun createViewModel(
        languageRepository: AppLanguageRepository = MutableLanguageRepository(AppLanguage.English),
    ): JourneyViewModel {
        return JourneyViewModel(
            ObserveJourneyUseCase(
                pregnancyRepository = pregnancyRepository,
                contentRepository = contentRepository,
                calculateProgress = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock),
                languageRepository = languageRepository,
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
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

        override suspend fun contentForWeek(week: Int, language: AppLanguage): WeekContent? = null

        override suspend fun allContent(language: AppLanguage): List<WeekContent> {
            requestedLanguages += language
            return (6..40).map { week ->
                WeekContent(
                    week = week,
                    babySize = BabySize.Lentil,
                    babySizeLabel = when (language) {
                        AppLanguage.English -> "a lentil"
                        AppLanguage.Spanish -> "una lenteja"
                    },
                    milestoneTitle = "Week $week",
                    keyEvents = listOf("Event"),
                )
            }
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
