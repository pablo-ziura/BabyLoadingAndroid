package com.pablo.ruiz.babyloading.feature.widget.domain.usecase

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetStateMapper
import com.pablo.ruiz.babyloading.feature.widget.domain.repository.WidgetRefreshRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareBabyProgressWidgetUseCaseTest {
    private val currentDate = LocalDate.of(2026, 8, 15)
    private val stateMapper = BabyProgressWidgetStateMapper(
        CalculatePregnancyProgressUseCase(
            PregnancyCalculator(),
            Clock.fixed(Instant.parse("2026-08-15T12:00:00Z"), ZoneOffset.UTC),
        ),
    )

    @Test
    fun ongoingContentIsLoadedExclusivelyForWeeksSixThroughForty() = runTest {
        val requestedWeeks = mutableListOf<Int>()
        val contentRepository = RecordingPregnancyContentRepository(requestedWeeks)

        val weekFive = prepare(currentDate.minusWeeks(5), contentRepository)
        val weekSix = prepare(currentDate.minusWeeks(6), contentRepository)
        val weekForty = prepare(currentDate.minusWeeks(40), contentRepository)
        val weekFortyOne = prepare(currentDate.minusWeeks(41), contentRepository)

        assertNull(weekFive.weekContent)
        assertEquals(6, weekSix.weekContent?.week)
        assertEquals(40, weekForty.weekContent?.week)
        assertNull(weekFortyOne.weekContent)
        assertEquals(listOf(6, 40), requestedWeeks)
    }

    @Test
    fun setupAndFutureStatesCancelRefreshWithoutLoadingContent() = runTest {
        val requestedWeeks = mutableListOf<Int>()
        val refreshRepository = RecordingWidgetRefreshRepository()
        val contentRepository = RecordingPregnancyContentRepository(requestedWeeks)

        val setup = prepare(null, contentRepository, refreshRepository)
        val future = prepare(currentDate.plusDays(1), contentRepository, refreshRepository)

        assertSame(BabyProgressWidgetState.NeedsSetup, setup.state)
        assertSame(BabyProgressWidgetState.InvalidFutureLastPeriodDate, future.state)
        assertTrue(requestedWeeks.isEmpty())
        assertEquals(
            listOf(
                BabyProgressWidgetState.NeedsSetup,
                BabyProgressWidgetState.InvalidFutureLastPeriodDate,
            ),
            refreshRepository.synchronizedStates,
        )
    }

    @Test
    fun storedDateFailureFallsBackToSetupAndSynchronizesCancellation() = runTest {
        val refreshRepository = RecordingWidgetRefreshRepository()
        val useCase = PrepareBabyProgressWidgetUseCase(
            pregnancyRepository = ThrowingPregnancyRepository,
            stateMapper = stateMapper,
            pregnancyContentRepository = RecordingPregnancyContentRepository(mutableListOf()),
            appLanguageRepository = EnglishAppLanguageRepository,
            refreshRepository = refreshRepository,
        )

        val prepared = useCase()

        assertSame(BabyProgressWidgetState.NeedsSetup, prepared.state)
        assertEquals(listOf(BabyProgressWidgetState.NeedsSetup), refreshRepository.synchronizedStates)
    }

    private suspend fun prepare(
        lastPeriodDate: LocalDate?,
        contentRepository: PregnancyContentRepository,
        refreshRepository: RecordingWidgetRefreshRepository = RecordingWidgetRefreshRepository(),
    ) = PrepareBabyProgressWidgetUseCase(
        pregnancyRepository = FakePregnancyRepository(lastPeriodDate),
        stateMapper = stateMapper,
        pregnancyContentRepository = contentRepository,
        appLanguageRepository = EnglishAppLanguageRepository,
        refreshRepository = refreshRepository,
    )()
}

private class FakePregnancyRepository(lastPeriodDate: LocalDate?) : PregnancyRepository {
    override val lastPeriodDate = MutableStateFlow(lastPeriodDate)

    override suspend fun setLastPeriodDate(date: LocalDate) {
        lastPeriodDate.value = date
    }

    override suspend fun clearLastPeriodDate() {
        lastPeriodDate.value = null
    }
}

private object ThrowingPregnancyRepository : PregnancyRepository {
    override val lastPeriodDate: Flow<LocalDate?> = emptyFlow()

    override suspend fun setLastPeriodDate(date: LocalDate) = Unit

    override suspend fun clearLastPeriodDate() = Unit
}

private class RecordingPregnancyContentRepository(
    private val requestedWeeks: MutableList<Int>,
) : PregnancyContentRepository {
    override suspend fun contentForWeek(week: Int, language: AppLanguage): WeekContent {
        requestedWeeks += week
        return WeekContent(
            week = week,
            babySize = BabySize.Lentil,
            babySizeLabel = "lentil",
            milestoneTitle = "milestone",
            keyEvents = emptyList(),
        )
    }

    override suspend fun allContent(language: AppLanguage): List<WeekContent> = emptyList()
}

private object EnglishAppLanguageRepository : AppLanguageRepository {
    override val changes: Flow<AppLanguage> = emptyFlow()

    override fun currentLanguage(): AppLanguage = AppLanguage.English

    override suspend fun refreshIfChanged(): Boolean = false
}

private class RecordingWidgetRefreshRepository : WidgetRefreshRepository {
    val synchronizedStates = mutableListOf<BabyProgressWidgetState>()

    override fun synchronize(state: BabyProgressWidgetState) {
        synchronizedStates += state
    }

    override fun cancel() = Unit
}
