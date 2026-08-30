package com.pablo.ruiz.babyloading.feature.dashboard.domain.model

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress

data class DashboardData(
    val progress: PregnancyProgress? = null,
    val weekContent: WeekContent? = null,
)
