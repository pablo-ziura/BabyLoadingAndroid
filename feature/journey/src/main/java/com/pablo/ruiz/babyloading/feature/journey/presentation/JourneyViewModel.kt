package com.pablo.ruiz.babyloading.feature.journey.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.journey.domain.usecase.ObserveJourneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class JourneyViewModel @Inject constructor(
    observeJourney: ObserveJourneyUseCase,
) : ViewModel() {
    private val foregroundSignals = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val uiState: StateFlow<JourneyUiState> = observeJourney(foregroundSignals)
        .map { data ->
            JourneyUiState(
                isLoading = false,
                isConfigured = data.isConfigured,
                currentWeek = data.currentWeek,
                currentDay = data.currentDay,
                weeks = data.weeks.map { content ->
                    JourneyWeekUiModel(
                        week = content.week,
                        status = when {
                            data.currentWeek != null && content.week < data.currentWeek -> {
                                JourneyWeekStatus.Completed
                            }
                            content.week == data.currentWeek -> JourneyWeekStatus.Current
                            else -> JourneyWeekStatus.Upcoming
                        },
                        content = content,
                    )
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = JourneyUiState(),
        )

    fun onEvent(event: JourneyEvent) {
        when (event) {
            JourneyEvent.Refresh -> foregroundSignals.tryEmit(Unit)
        }
    }
}
