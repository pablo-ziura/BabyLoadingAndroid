package com.pablo.ruiz.babyloading.core.localization

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageMapperTest {
    private val mapper = AppLanguageMapper()

    @Test
    fun perAppLanguageTakesPriorityOverTheDeviceLanguage() {
        assertEquals(
            AppLanguage.Spanish,
            mapper.map(
                applicationLocales = listOf(Locale.forLanguageTag("es-ES")),
                deviceLocales = listOf(Locale.ENGLISH),
            ),
        )
    }

    @Test
    fun deviceLanguageIsUsedWhenAndroidHasNoPerAppLanguage() {
        assertEquals(
            AppLanguage.Spanish,
            mapper.map(
                applicationLocales = emptyList(),
                deviceLocales = listOf(Locale.forLanguageTag("es-MX")),
            ),
        )
    }

    @Test
    fun unsupportedLanguagesFallBackToEnglish() {
        assertEquals(
            AppLanguage.English,
            mapper.map(
                applicationLocales = listOf(Locale.FRENCH),
                deviceLocales = listOf(Locale.GERMAN),
            ),
        )
    }
}
