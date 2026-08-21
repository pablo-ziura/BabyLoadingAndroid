package com.pablo.ruiz.babyloading.core.localization

import java.util.Locale

/**
 * The languages supported by the app's bundled editorial content and widgets.
 */
enum class AppLanguage(
    val languageTag: String,
) {
    English("en"),
    Spanish("es"),
    ;

    companion object {
        fun from(locale: Locale): AppLanguage? = entries.firstOrNull { language ->
            language.languageTag == locale.language.lowercase(Locale.ROOT)
        }
    }
}
