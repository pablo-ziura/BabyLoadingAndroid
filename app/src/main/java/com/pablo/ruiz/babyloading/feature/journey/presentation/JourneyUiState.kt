package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage

data class JourneyUiState(
    val isLoading: Boolean = true,
    val currentWeek: Int? = null,
    val currentDay: Int = 0,
    val stage: PregnancyStage? = null,
    val weeks: List<JourneyWeekUiModel> = emptyList(),
    val expandedWeek: Int? = null,
)

data class JourneyWeekUiModel(
    val week: Int,
    val status: JourneyWeekStatus,
    val content: WeekContent?,
)

enum class JourneyWeekStatus {
    Completed,
    Current,
    Upcoming,
}

sealed interface JourneyEvent {
    data class WeekSelected(val week: Int) : JourneyEvent

    data object Refresh : JourneyEvent
}
