package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.localization.AppLocaleProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
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
    fun weeksPastTheGuideMarkEveryEditorialWeekCompleted() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(43)
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(35, state.weeks.count { it.status == JourneyWeekStatus.Completed })
        assertEquals(0, state.weeks.count { it.status == JourneyWeekStatus.Current })
    }

    private fun createViewModel(): JourneyViewModel {
        return JourneyViewModel(
            pregnancyRepository = pregnancyRepository,
            contentRepository = contentRepository,
            calculateProgress = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock),
            localeProvider = object : AppLocaleProvider {
                override fun currentLocale(): Locale = Locale.ENGLISH
            },
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
        override fun contentForWeek(week: Int, locale: Locale): WeekContent? = null

        override fun allContent(locale: Locale): List<WeekContent> {
            return (6..40).map { week ->
                WeekContent(
                    week = week,
                    babySize = BabySize.Lentil,
                    babySizeLabel = "a lentil",
                    milestoneTitle = "Week $week",
                    keyEvents = listOf("Event"),
                )
            }
        }
    }
}
