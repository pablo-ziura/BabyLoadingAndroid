package com.pablo.ruiz.babyloading.core.pregnancy.domain

import java.time.LocalDate

class PregnancyDateValidator {
    fun validate(
        lastPeriodDate: LocalDate,
        currentDate: LocalDate,
    ): PregnancyDateValidation {
        return if (lastPeriodDate.isAfter(currentDate)) {
            PregnancyDateValidation.FutureDate
        } else {
            PregnancyDateValidation.Valid
        }
    }
}

enum class PregnancyDateValidation {
    Valid,
    FutureDate,
}
