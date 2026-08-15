package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
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

    @Test
    fun everyBabySizeMapsToADistinctDrawable() {
        val resources = BabySize.entries.map(BabySize::drawableResource)

        assertEquals(BabySize.entries.size, resources.distinct().size)
    }
}
