package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentParser
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentMapper
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PregnancyContentResourceTest {
    private val parser = PregnancyContentParser(
        json = Json { ignoreUnknownKeys = true },
        mapper = PregnancyContentMapper(),
    )

    @Test
    fun bundledEnglishAndSpanishDocumentsAreCompleteAndValid() {
        listOf("en", "es").forEach { locale ->
            val content = File("src/main/assets/pregnancy-content.$locale.json").readText()

            val document = parser.parse(content, expectedLocale = locale)

            assertEquals(locale, document.locale)
            assertEquals(PregnancyContentDocument.CoveredWeeks, document.weeks.map { it.week })
            assertEquals(35, document.weeks.map { it.babySize }.distinct().size)
        }
    }

    @Test
    fun unknownBabySizeFailsDecoding() {
        val invalidJson = """
            {
              "schemaVersion": 1,
              "locale": "en",
              "revision": 1,
              "weeks": [{
                "week": 6,
                "babySize": "dragonEgg",
                "babySizeLabel": "a dragon egg",
                "milestoneTitle": "Week 6",
                "keyEvents": ["Event"]
              }]
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            parser.parse(invalidJson, expectedLocale = "en")
        }
    }

    @Test
    fun everyBabySizeMapsToADistinctDrawable() {
        val resources = BabySize.entries.map(BabySize::drawableResource)

        assertEquals(BabySize.entries.size, resources.distinct().size)
    }
}
