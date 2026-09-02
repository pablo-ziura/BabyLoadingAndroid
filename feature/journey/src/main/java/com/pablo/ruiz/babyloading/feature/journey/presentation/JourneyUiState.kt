package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
data class JourneyUiState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val currentWeek: Int? = null,
    val currentDay: Int = 0,
    val weeks: List<JourneyWeekUiModel> = emptyList(),
) {
    fun currentDayTimelineWeek(): Int? {
        val currentWeek = currentWeek ?: return null
        val dayTimelineWeek = currentWeek + if (currentDay >= 4) 1 else 0

        return when {
            weeks.any { it.week == dayTimelineWeek } -> dayTimelineWeek
            weeks.any { it.week == currentWeek } -> currentWeek
            else -> null
        }
    }
}

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
