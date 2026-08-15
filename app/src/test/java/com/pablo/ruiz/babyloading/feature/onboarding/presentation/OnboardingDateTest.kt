package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingDateTest {
    @Test
    fun datePickerUtcMillisRoundTripWithoutTimezoneConversion() {
        val date = LocalDate.of(2026, 5, 10)

        val restoredDate = date.toUtcDatePickerMillis().toLocalDateFromDatePicker()

        assertEquals(date, restoredDate)
    }

    @Test
    fun dateFormattingUsesRequestedLocale() {
        val date = LocalDate.of(2026, 5, 10)

        assertEquals("May 10, 2026", OnboardingDateFormatter.format(date, Locale.US))
        assertEquals("10 de mayo de 2026", OnboardingDateFormatter.format(date, Locale.forLanguageTag("es-ES")))
    }
}
