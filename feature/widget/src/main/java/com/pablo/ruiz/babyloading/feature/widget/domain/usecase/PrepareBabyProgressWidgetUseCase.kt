package com.pablo.ruiz.babyloading.feature.widget.domain.usecase

import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetStateMapper
import com.pablo.ruiz.babyloading.feature.widget.domain.PreparedBabyProgressWidget
import com.pablo.ruiz.babyloading.feature.widget.domain.repository.WidgetRefreshRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class PrepareBabyProgressWidgetUseCase @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val stateMapper: BabyProgressWidgetStateMapper,
    private val pregnancyContentRepository: PregnancyContentRepository,
    private val appLanguageRepository: AppLanguageRepository,
    private val refreshRepository: WidgetRefreshRepository,
) {
    suspend operator fun invoke(): PreparedBabyProgressWidget {
        val state = loadState()
        refreshRepository.synchronize(state)
        return PreparedBabyProgressWidget(
            state = state,
            weekContent = loadOngoingContent(state),
        )
    }

    private suspend fun loadState(): BabyProgressWidgetState {
        return try {
            stateMapper.map(pregnancyRepository.lastPeriodDate.first())
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            BabyProgressWidgetState.NeedsSetup
        }
    }

    private suspend fun loadOngoingContent(state: BabyProgressWidgetState): WeekContent? {
        val week = (state as? BabyProgressWidgetState.Ongoing)
            ?.progress
            ?.gestationalAge
            ?.completedWeeks
            ?.takeIf { it in ContentWeekRange }
            ?: return null
        return try {
            pregnancyContentRepository.contentForWeek(
                week = week,
                language = appLanguageRepository.currentLanguage(),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        val ContentWeekRange = 6..40
    }
}
