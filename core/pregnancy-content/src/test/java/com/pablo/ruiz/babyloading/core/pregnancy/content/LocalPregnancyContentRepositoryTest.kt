package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.LocalPregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentDataSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest

class LocalPregnancyContentRepositoryTest {
    private val dataSource = FakeContentDataSource()
    private val repository = LocalPregnancyContentRepository(dataSource = dataSource)

    @Test
    fun languageSelectsTheMatchingBundledContent() = runTest {
        assertEquals("es", repository.allContent(AppLanguage.Spanish).first().babySizeLabel)
        assertEquals("en", repository.allContent(AppLanguage.English).first().babySizeLabel)
    }

    @Test
    fun onlyCoveredWeeksHaveEditorialContent() = runTest {
        assertNull(repository.contentForWeek(5, AppLanguage.English))
        assertNull(repository.contentForWeek(41, AppLanguage.English))
        assertNull(repository.contentForWeek(42, AppLanguage.English))
    }

    @Test
    fun documentsAreCachedPerLanguage() = runTest {
        repository.allContent(AppLanguage.English)
        repository.allContent(AppLanguage.English)
        repository.allContent(AppLanguage.Spanish)

        assertEquals(listOf("en", "es"), dataSource.loadedLocales)
    }

    @Test
    fun concurrentRequestsLoadEachLanguageOnlyOnce() = runTest {
        coroutineScope {
            List(20) {
                async { repository.allContent(AppLanguage.English) }
            }.awaitAll()
        }

        assertEquals(listOf("en"), dataSource.loadedLocales)
    }

    private class FakeContentDataSource : PregnancyContentDataSource {
        val loadedLocales = mutableListOf<String>()

        override suspend fun load(localeCode: String): PregnancyContentDocument {
            loadedLocales += localeCode
            return PregnancyContentDocument(
                schemaVersion = 1,
                locale = localeCode,
                revision = 1,
                weeks = PregnancyContentDocument.CoveredWeeks.map { week ->
                    WeekContent(
                        week = week,
                        babySize = BabySize.Lentil,
                        babySizeLabel = localeCode,
                        milestoneTitle = "Week $week",
                        keyEvents = listOf("Event"),
                    )
                },
            )
        }
    }
}
