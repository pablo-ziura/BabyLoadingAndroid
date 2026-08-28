package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
data class JourneyUiState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val currentWeek: Int? = null,
    val currentDay: Int = 0,
    val weeks: List<JourneyWeekUiModel> = emptyList(),
)

data class JourneyWeekUiModel(
    val week: Int,
    val status: JourneyWeekStatus,
    val content: WeekContent,
)

enum class JourneyWeekStatus {
    Completed,
    Current,
    Upcoming,
}

sealed interface JourneyEvent {
    data object Refresh : JourneyEvent
}
