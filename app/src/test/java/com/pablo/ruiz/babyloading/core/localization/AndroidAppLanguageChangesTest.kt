package com.pablo.ruiz.babyloading.core.localization

import kotlinx.coroutines.async
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAppLanguageChangesTest {
    @Test
    fun foregroundRefreshEmitsOnlyWhenTheEffectiveLanguageChanges() = runTest {
        val languageProvider = MutableLanguageProvider(AppLanguage.English)
        val changes = AndroidAppLanguageChanges(languageProvider)

        assertFalse(changes.refreshIfLanguageChanged())

        languageProvider.language = AppLanguage.Spanish
        val emittedLanguage = async { changes.changes.first() }
        runCurrent()

        assertTrue(changes.refreshIfLanguageChanged())
        assertEquals(AppLanguage.Spanish, emittedLanguage.await())
        assertFalse(changes.refreshIfLanguageChanged())
    }

    private class MutableLanguageProvider(
        var language: AppLanguage,
    ) : AppLanguageProvider {
        override fun currentLanguage(): AppLanguage = language
    }
}
