package com.pablo.ruiz.babyloading.feature.journey.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.localization.AppLocaleProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val contentRepository: PregnancyContentRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val localeProvider: AppLocaleProvider,
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
    }

    fun onEvent(event: JourneyEvent) {
        when (event) {
            is JourneyEvent.WeekSelected -> _uiState.update { state ->
                state.copy(
                    expandedWeek = if (state.expandedWeek == event.week) null else event.week,
                )
            }
            JourneyEvent.Refresh -> viewModelScope.launch { load(lastPeriodDate) }
        }
    }

    private suspend fun load(date: LocalDate?) {
        if (date == null) {
            _uiState.value = JourneyUiState(isLoading = false)
            return
        }

        val expandedWeek = _uiState.value.expandedWeek
        _uiState.value = withContext(ioDispatcher) {
            val progress = calculateProgress(date)
            val contentByWeek = contentRepository
                .allContent(localeProvider.currentLocale())
                .associateBy { content -> content.week }
            val currentWeek = progress.gestationalAge.completedWeeks

            JourneyUiState(
                isLoading = false,
                currentWeek = currentWeek,
                currentDay = progress.gestationalAge.daysIntoWeek,
                stage = progress.stage,
                weeks = (1..PregnancyCalculator.LAST_JOURNEY_WEEK).map { week ->
                    JourneyWeekUiModel(
                        week = week,
                        status = when {
                            week < currentWeek -> JourneyWeekStatus.Completed
                            week == currentWeek -> JourneyWeekStatus.Current
                            else -> JourneyWeekStatus.Upcoming
                        },
                        content = contentByWeek[week],
                    )
                },
                expandedWeek = expandedWeek,
            )
        }
    }
}
