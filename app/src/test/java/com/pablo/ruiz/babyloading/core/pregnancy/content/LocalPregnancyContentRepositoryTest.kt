package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.LocalPregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalPregnancyContentRepositoryTest {
    private val source = FakeContentSource()
    private val repository = LocalPregnancyContentRepository(source = source)

    @Test
    fun languageSelectsTheMatchingBundledContent() {
        assertEquals("es", repository.allContent(AppLanguage.Spanish).first().babySizeLabel)
        assertEquals("en", repository.allContent(AppLanguage.English).first().babySizeLabel)
    }

    @Test
    fun earlyWeeksHaveNoEditorialContentAndLateWeeksReuseWeekForty() {
        assertNull(repository.contentForWeek(5, AppLanguage.English))
        assertEquals(40, repository.contentForWeek(41, AppLanguage.English)?.week)
        assertEquals(40, repository.contentForWeek(42, AppLanguage.English)?.week)
    }

    @Test
    fun documentsAreCachedPerLanguage() {
        repository.allContent(AppLanguage.English)
        repository.allContent(AppLanguage.English)
        repository.allContent(AppLanguage.Spanish)

        assertEquals(listOf("en", "es"), source.loadedLocales)
    }

    private class FakeContentSource : PregnancyContentSource {
        val loadedLocales = mutableListOf<String>()

        override fun load(localeCode: String): PregnancyContentDocument {
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
