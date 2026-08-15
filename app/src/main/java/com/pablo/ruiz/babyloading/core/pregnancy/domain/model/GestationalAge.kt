package com.pablo.ruiz.babyloading.core.pregnancy.domain.model

data class GestationalAge(
    val completedWeeks: Int,
    val daysIntoWeek: Int,
    val elapsedDays: Int,
) {
    init {
        require(completedWeeks >= 0) { "Completed weeks cannot be negative" }
        require(daysIntoWeek in 0..6) { "Days into a week must be between 0 and 6" }
        require(elapsedDays >= 0) { "Elapsed days cannot be negative" }
    }
}
