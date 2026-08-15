package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import com.pablo.ruiz.babyloading.core.localization.AppLocaleProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage
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
        assertEquals(20, state.progress?.gestationalAge?.completedWeeks)
        assertEquals(PregnancyStage.Active, state.progress?.stage)
        assertEquals(20, state.weekContent?.week)
        assertEquals(Locale.forLanguageTag("es-ES"), contentRepository.requestedLocale)
    }

    @Test
    fun earlyPregnancyHasNoWeeklyEditorialEntry() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(5)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(PregnancyStage.Early, viewModel.uiState.value.progress?.stage)
        assertNull(viewModel.uiState.value.weekContent)
    }

    @Test
    fun postTermAndReviewStagesRemainDistinct() = runTest {
        pregnancyRepository.date.value = currentDate.minusWeeks(41)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(PregnancyStage.PostTerm, viewModel.uiState.value.progress?.stage)
        assertEquals(40, viewModel.uiState.value.weekContent?.week)

        pregnancyRepository.date.value = currentDate.minusWeeks(43)
        advanceUntilIdle()
        assertEquals(PregnancyStage.NeedsReview, viewModel.uiState.value.progress?.stage)
        assertEquals(40, viewModel.uiState.value.weekContent?.week)
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            pregnancyRepository = pregnancyRepository,
            contentRepository = contentRepository,
            calculateProgress = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock),
            localeProvider = object : AppLocaleProvider {
                override fun currentLocale(): Locale = Locale.forLanguageTag("es-ES")
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
        var requestedLocale: Locale? = null

        override fun contentForWeek(week: Int, locale: Locale): WeekContent? {
            requestedLocale = locale
            if (week < 6) return null
            val contentWeek = week.coerceAtMost(40)
            return WeekContent(
                week = contentWeek,
                babySize = BabySize.Lentil,
                babySizeLabel = "una lenteja",
                milestoneTitle = "Semana $contentWeek",
                keyEvents = listOf("Evento"),
            )
        }

        override fun allContent(locale: Locale): List<WeekContent> = emptyList()
    }
}
