package com.pablo.ruiz.babyloading.core.localization

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageResolverTest {
    private val resolver = AppLanguageResolver()

    @Test
    fun perAppLanguageTakesPriorityOverTheDeviceLanguage() {
        assertEquals(
            AppLanguage.Spanish,
            resolver.resolve(
                applicationLocales = listOf(Locale.forLanguageTag("es-ES")),
                deviceLocales = listOf(Locale.ENGLISH),
            ),
        )
    }

    @Test
    fun deviceLanguageIsUsedWhenAndroidHasNoPerAppLanguage() {
        assertEquals(
            AppLanguage.Spanish,
            resolver.resolve(
                applicationLocales = emptyList(),
                deviceLocales = listOf(Locale.forLanguageTag("es-MX")),
            ),
        )
    }

    @Test
    fun unsupportedLanguagesFallBackToEnglish() {
        assertEquals(
            AppLanguage.English,
            resolver.resolve(
                applicationLocales = listOf(Locale.FRENCH),
                deviceLocales = listOf(Locale.GERMAN),
            ),
        )
    }
}
