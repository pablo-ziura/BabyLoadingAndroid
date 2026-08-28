package com.pablo.ruiz.babyloading.core.localization

import java.util.Locale
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
class DefaultAppLanguageRepositoryTest {
    @Test
    fun foregroundRefreshEmitsOnlyWhenTheEffectiveLanguageChanges() = runTest {
        val dataSource = MutableAppLanguageDataSource(Locale.ENGLISH)
        val repository = DefaultAppLanguageRepository(dataSource, AppLanguageMapper())

        assertFalse(repository.refreshIfChanged())

        dataSource.applicationLocale = Locale.forLanguageTag("es-ES")
        val emittedLanguage = async { repository.changes.first() }
        runCurrent()

        assertTrue(repository.refreshIfChanged())
        assertEquals(AppLanguage.Spanish, emittedLanguage.await())
        assertFalse(repository.refreshIfChanged())
    }

    private class MutableAppLanguageDataSource(
        var applicationLocale: Locale,
    ) : AppLanguageDataSource {
        override fun applicationLocales(): List<Locale> = listOf(applicationLocale)

        override fun deviceLocales(): List<Locale> = emptyList()
    }
}
