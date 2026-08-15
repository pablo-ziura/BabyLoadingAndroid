package com.pablo.ruiz.babyloading.core.localization

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface AppLocaleProvider {
    fun currentLocale(): Locale
}

@Singleton
class AndroidAppLocaleProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppLocaleProvider {
    override fun currentLocale(): Locale {
        return context.resources.configuration.locales[0] ?: Locale.ENGLISH
    }
}
