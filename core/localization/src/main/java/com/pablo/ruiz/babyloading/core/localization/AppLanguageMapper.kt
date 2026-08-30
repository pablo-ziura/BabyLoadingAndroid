package com.pablo.ruiz.babyloading.core.localization

import java.util.Locale
import javax.inject.Inject

class AppLanguageMapper @Inject constructor() {
    fun map(
        applicationLocales: List<Locale>,
        deviceLocales: List<Locale>,
    ): AppLanguage {
        return applicationLocales.firstNotNullOfOrNull(AppLanguage::from)
            ?: deviceLocales.firstNotNullOfOrNull(AppLanguage::from)
            ?: AppLanguage.English
    }
}
