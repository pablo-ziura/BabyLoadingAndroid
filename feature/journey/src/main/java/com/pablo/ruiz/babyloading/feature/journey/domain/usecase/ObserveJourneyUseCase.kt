package com.pablo.ruiz.babyloading.feature.journey.domain.usecase

import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.journey.domain.model.JourneyData
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class ObserveJourneyUseCase @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val contentRepository: PregnancyContentRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val languageRepository: AppLanguageRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(foregroundSignals: Flow<Unit>): Flow<JourneyData> {
        val languages = languageRepository.changes
            .onStart { emit(languageRepository.currentLanguage()) }
            .distinctUntilChanged()

        return combine(
            pregnancyRepository.lastPeriodDate,
            languages,
            foregroundSignals.onStart { emit(Unit) },
        ) { lastPeriodDate, language, _ ->
            lastPeriodDate to language
        }.mapLatest { (lastPeriodDate, language) ->
            if (lastPeriodDate == null) {
                JourneyData()
            } else {
                withContext(ioDispatcher) {
                    val progress = calculateProgress(lastPeriodDate)
                    val timelineProgress = (progress as? PregnancyProgress.Active)
                        ?.progress
                        ?.takeIf { it.phase == PregnancyPhase.Ongoing }
                    JourneyData(
                        isConfigured = true,
                        currentWeek = timelineProgress?.gestationalAge?.completedWeeks,
                        currentDay = timelineProgress?.gestationalAge?.daysIntoWeek ?: 0,
                        weeks = contentRepository.allContent(language).sortedBy { it.week },
                    )
                }
            }
        }
    }
}
