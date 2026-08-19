package com.pablo.ruiz.babyloading.feature.journey.presentation

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class JourneyPresentationTest {
    @Test
    fun babySizeLabelsUseTheIosCapitalization() {
        assertEquals("A Bunch Of Grapes", formatJourneyBabySizeLabel("a bunch of grapes", Locale.ENGLISH))
        assertEquals("Una Lenteja", formatJourneyBabySizeLabel("una lenteja", Locale.forLanguageTag("es-ES")))
        assertEquals("Un Arándano", formatJourneyBabySizeLabel("un arándano", Locale.forLanguageTag("es-ES")))
    }
}
