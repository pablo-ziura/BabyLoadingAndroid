package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress

data class DashboardUiState(
    val isLoading: Boolean = true,
    val progress: PregnancyProgress? = null,
    val weekContent: WeekContent? = null,
)

sealed interface DashboardEvent {
    data object Refresh : DashboardEvent
}
