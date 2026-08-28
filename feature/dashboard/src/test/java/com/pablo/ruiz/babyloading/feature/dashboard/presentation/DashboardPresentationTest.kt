package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardPresentationTest {
    @Test
    fun dueDateFormattingUsesRequestedLocale() {
        val date = LocalDate.of(2026, 12, 6)

        assertEquals("Dec 6, 2026", DashboardDateFormatter.format(date, Locale.US))
        assertEquals("6 dic 2026", DashboardDateFormatter.format(date, Locale.forLanguageTag("es-ES")))
    }
}
