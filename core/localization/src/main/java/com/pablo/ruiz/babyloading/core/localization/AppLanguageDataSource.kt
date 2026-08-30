package com.pablo.ruiz.babyloading.core.localization

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface AppLanguageDataSource {
    fun applicationLocales(): List<Locale>

    fun deviceLocales(): List<Locale>
}

@Singleton
class AndroidAppLanguageDataSource @Inject constructor(
    @ApplicationContext context: Context,
) : AppLanguageDataSource {
    private val localeManager = requireNotNull(context.getSystemService(LocaleManager::class.java))

    override fun applicationLocales(): List<Locale> = localeManager.applicationLocales.toLocales()

    override fun deviceLocales(): List<Locale> = localeManager.systemLocales.toLocales()
}

private fun LocaleList.toLocales(): List<Locale> = List(size()) { index -> get(index) }
