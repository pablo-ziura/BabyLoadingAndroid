package com.pablo.ruiz.babyloading.feature.journey.domain.model

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent

data class JourneyData(
    val isConfigured: Boolean = false,
    val currentWeek: Int? = null,
    val currentDay: Int = 0,
    val weeks: List<WeekContent> = emptyList(),
)
