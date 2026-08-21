package com.pablo.ruiz.babyloading.feature.journey.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.localization.AppLanguageChanges
import com.pablo.ruiz.babyloading.core.localization.AppLanguageProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val contentRepository: PregnancyContentRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val languageProvider: AppLanguageProvider,
    private val languageChanges: AppLanguageChanges,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    private var lastPeriodDate: LocalDate? = null

    init {
        viewModelScope.launch {
            pregnancyRepository.lastPeriodDate.collectLatest { date ->
                lastPeriodDate = date
                load(date)
            }
        }
        viewModelScope.launch {
            languageChanges.changes.collectLatest {
                load(lastPeriodDate)
            }
        }
    }

    fun onEvent(event: JourneyEvent) {
        when (event) {
            JourneyEvent.Refresh -> viewModelScope.launch { load(lastPeriodDate) }
        }
    }

    private suspend fun load(date: LocalDate?) {
        if (date == null) {
            _uiState.value = JourneyUiState(isLoading = false)
            return
        }

        _uiState.value = withContext(ioDispatcher) {
            val progress = calculateProgress(date)
            val weeklyContent = contentRepository
                .allContent(languageProvider.currentLanguage())
                .sortedBy { content -> content.week }
            val currentWeek = progress.gestationalAge.completedWeeks

            JourneyUiState(
                isLoading = false,
                currentWeek = currentWeek,
                currentDay = progress.gestationalAge.daysIntoWeek,
                weeks = weeklyContent.map { content ->
                    JourneyWeekUiModel(
                        week = content.week,
                        status = when {
                            content.week < currentWeek -> JourneyWeekStatus.Completed
                            content.week == currentWeek -> JourneyWeekStatus.Current
                            else -> JourneyWeekStatus.Upcoming
                        },
                        content = content,
                    )
                },
            )
        }
    }
}
