package com.pablo.ruiz.babyloading.core.pregnancy.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PregnancyDateValidatorTest {
    private val validator = PregnancyDateValidator()
    private val today = LocalDate.of(2026, 8, 15)

    @Test
    fun todayAndPastDatesAreValid() {
        assertEquals(PregnancyDateValidation.Valid, validator.validate(today, today))
        assertEquals(PregnancyDateValidation.Valid, validator.validate(today.minusDays(301), today))
    }

    @Test
    fun futureDateIsRejected() {
        assertEquals(
            PregnancyDateValidation.FutureDate,
            validator.validate(today.plusDays(1), today),
        )
    }
}
