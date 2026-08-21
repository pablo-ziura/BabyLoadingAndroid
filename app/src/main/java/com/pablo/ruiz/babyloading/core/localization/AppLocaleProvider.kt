package com.pablo.ruiz.babyloading.core.localization

import android.content.Context
import android.app.LocaleManager
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale

/**
 * Read-only access to Android's effective per-app language.
 */
interface AppLanguageProvider {
    fun currentLanguage(): AppLanguage
}

@Singleton
class AndroidAppLocaleProvider @Inject constructor(
    @ApplicationContext context: Context,
    private val resolver: AppLanguageResolver,
) : AppLanguageProvider {
    private val localeManager = requireNotNull(context.getSystemService(LocaleManager::class.java))

    override fun currentLanguage(): AppLanguage {
        return resolver.resolve(
            applicationLocales = localeManager.applicationLocales.toLocales(),
            deviceLocales = localeManager.systemLocales.toLocales(),
        )
    }
}

class AppLanguageResolver @Inject constructor() {
    fun resolve(
        applicationLocales: List<Locale>,
        deviceLocales: List<Locale>,
    ): AppLanguage {
        return applicationLocales.firstNotNullOfOrNull(AppLanguage::from)
            ?: deviceLocales.firstNotNullOfOrNull(AppLanguage::from)
            ?: AppLanguage.English
    }
}

private fun LocaleList.toLocales(): List<Locale> = List(size()) { index -> get(index) }
