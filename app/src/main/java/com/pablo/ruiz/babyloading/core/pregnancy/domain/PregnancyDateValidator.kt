package com.pablo.ruiz.babyloading.core.pregnancy.domain

import java.time.LocalDate

class PregnancyDateValidator {
    fun validate(
        lastPeriodDate: LocalDate,
        currentDate: LocalDate,
    ): PregnancyDateValidation {
        return when {
            lastPeriodDate.isAfter(currentDate) -> PregnancyDateValidation.FutureDate
            lastPeriodDate.isBefore(currentDate.minusWeeks(MaximumPastWeeks.toLong())) -> {
                PregnancyDateValidation.DateTooOld
            }
            else -> PregnancyDateValidation.Valid
        }
    }

    companion object {
        const val MaximumPastWeeks = 42
    }
}

enum class PregnancyDateValidation {
    Valid,
    FutureDate,
    DateTooOld,
}
