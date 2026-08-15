package com.pablo.ruiz.babyloading.core.pregnancy.content.domain

import java.util.Locale
import javax.inject.Inject

class PregnancyContentLocaleResolver @Inject constructor() {
    fun resolve(locale: Locale): String {
        val language = locale.language.lowercase(Locale.ROOT)
        return if (language in SupportedLocales) language else FallbackLocale
    }

    companion object {
        const val FallbackLocale = "en"
        val SupportedLocales: Set<String> = setOf("en", "es")
    }
}
