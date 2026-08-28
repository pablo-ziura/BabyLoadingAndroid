package com.pablo.ruiz.babyloading.feature.settings.domain.usecase

import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.settings.domain.model.SettingsData
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

class ObserveSettingsUseCase @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val languageRepository: AppLanguageRepository,
    clock: Clock,
) {
    private val currentDate = LocalDate.now(clock)

    operator fun invoke(): Flow<SettingsData> {
        val languages = languageRepository.changes
            .onStart { emit(languageRepository.currentLanguage()) }
            .distinctUntilChanged()

        return combine(pregnancyRepository.lastPeriodDate, languages) { savedDate, language ->
            SettingsData(
                savedDate = savedDate,
                pregnancyProgress = savedDate?.let(calculateProgress::invoke),
                minimumDate = currentDate.minusWeeks(
                    PregnancyDateValidator.MaximumPastWeeks.toLong(),
                ),
                maximumDate = currentDate,
                appLanguage = language,
            )
        }
    }
}
