package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentValidationException
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PregnancyContentDocumentTest {
    @Test
    fun validDocumentIsNormalizedByWeek() {
        val document = validDocument().copy(weeks = validDocument().weeks.reversed())

        val validated = document.validated(expectedLocale = "en")

        assertEquals(PregnancyContentDocument.CoveredWeeks, validated.weeks.map(WeekContent::week))
    }

    @Test
    fun unsupportedSchemaAndLocaleAreRejected() {
        assertThrows(PregnancyContentValidationException.UnsupportedSchema::class.java) {
            validDocument().copy(schemaVersion = 2).validated("en")
        }
        assertThrows(PregnancyContentValidationException.UnsupportedLocale::class.java) {
            validDocument().copy(locale = "es").validated("en")
        }
    }

    @Test
    fun duplicateAndIncompleteWeeksAreRejected() {
        val weeks = validDocument().weeks
        assertThrows(PregnancyContentValidationException.DuplicateWeeks::class.java) {
            validDocument().copy(weeks = weeks + weeks.first()).validated("en")
        }
        assertThrows(PregnancyContentValidationException.InvalidWeekCoverage::class.java) {
            validDocument().copy(weeks = weeks.dropLast(1)).validated("en")
        }
    }

    @Test
    fun blankEditorialContentIsRejected() {
        val weeks = validDocument().weeks.toMutableList()
        weeks[0] = weeks[0].copy(keyEvents = listOf(" "))

        assertThrows(PregnancyContentValidationException.EmptyContent::class.java) {
            validDocument().copy(weeks = weeks).validated("en")
        }
    }

    private fun validDocument(): PregnancyContentDocument {
        val weeks = PregnancyContentDocument.CoveredWeeks.mapIndexed { index, week ->
            WeekContent(
                week = week,
                babySize = BabySize.entries[index],
                babySizeLabel = "Size $week",
                milestoneTitle = "Week $week",
                keyEvents = listOf("Event $week"),
                physiologicalImpact = "Impact $week",
            )
        }
        return PregnancyContentDocument(
            schemaVersion = 1,
            locale = "en",
            revision = 1,
            weeks = weeks,
        )
    }
}
