package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.PregnancyContentLocaleResolver
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class PregnancyContentLocaleResolverTest {
    private val resolver = PregnancyContentLocaleResolver()

    @Test
    fun supportedLanguageIsResolvedWithoutRegion() {
        assertEquals("es", resolver.resolve(Locale.forLanguageTag("es-ES")))
        assertEquals("en", resolver.resolve(Locale.forLanguageTag("en-GB")))
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglish() {
        assertEquals("en", resolver.resolve(Locale.FRENCH))
    }
}
