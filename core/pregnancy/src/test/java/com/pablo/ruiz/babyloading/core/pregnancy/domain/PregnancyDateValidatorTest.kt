package com.pablo.ruiz.babyloading.core.pregnancy.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PregnancyDateValidatorTest {
    private val validator = PregnancyDateValidator()
    private val today = LocalDate.of(2026, 8, 15)

    @Test
    fun todayAndFortyTwoWeeksAgoAreValid() {
        assertEquals(PregnancyDateValidation.Valid, validator.validate(today, today))
        assertEquals(PregnancyDateValidation.Valid, validator.validate(today.minusWeeks(42), today))
    }

    @Test
    fun futureDateIsRejected() {
        assertEquals(
            PregnancyDateValidation.FutureDate,
            validator.validate(today.plusDays(1), today),
        )
    }

    @Test
    fun datesOlderThanFortyTwoWeeksAreRejected() {
        assertEquals(
            PregnancyDateValidation.DateTooOld,
            validator.validate(today.minusWeeks(42).minusDays(1), today),
        )
    }
}
