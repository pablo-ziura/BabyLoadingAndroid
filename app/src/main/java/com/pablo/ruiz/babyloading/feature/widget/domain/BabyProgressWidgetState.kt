package com.pablo.ruiz.babyloading.feature.widget.domain

import java.time.LocalDate

sealed interface BabyProgressWidgetState {
    data object NeedsSetup : BabyProgressWidgetState

    data class Progress(
        val completedWeeks: Int,
        val daysIntoWeek: Int,
        val daysRemaining: Int,
        val completedFraction: Float,
        val estimatedDueDate: LocalDate,
    ) : BabyProgressWidgetState
}
