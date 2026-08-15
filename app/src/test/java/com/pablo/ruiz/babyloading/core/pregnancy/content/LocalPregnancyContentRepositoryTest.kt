package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.pregnancy.content.data.LocalPregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.PregnancyContentLocaleResolver
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalPregnancyContentRepositoryTest {
    private val source = FakeContentSource()
    private val repository = LocalPregnancyContentRepository(
        source = source,
        localeResolver = PregnancyContentLocaleResolver(),
    )

    @Test
    fun localeResolutionUsesSpanishAndFallsBackToEnglish() {
        assertEquals("es", repository.allContent(Locale.forLanguageTag("es-MX")).first().babySizeLabel)
        assertEquals("en", repository.allContent(Locale.FRENCH).first().babySizeLabel)
    }

    @Test
    fun earlyWeeksHaveNoEditorialContentAndLateWeeksReuseWeekForty() {
        assertNull(repository.contentForWeek(5, Locale.ENGLISH))
        assertEquals(40, repository.contentForWeek(41, Locale.ENGLISH)?.week)
        assertEquals(40, repository.contentForWeek(42, Locale.ENGLISH)?.week)
    }

    @Test
    fun documentsAreCachedPerResolvedLocale() {
        repository.allContent(Locale.US)
        repository.allContent(Locale.UK)
        repository.allContent(Locale.forLanguageTag("es-ES"))

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
