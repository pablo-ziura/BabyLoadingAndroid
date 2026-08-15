package com.pablo.ruiz.babyloading.core.pregnancy.content

import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentParser
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PregnancyContentResourceTest {
    private val parser = PregnancyContentParser(Json { ignoreUnknownKeys = true })

    @Test
    fun bundledEnglishAndSpanishDocumentsAreCompleteAndValid() {
        listOf("en", "es").forEach { locale ->
            val content = requireNotNull(
                javaClass.classLoader?.getResourceAsStream("pregnancy-content.$locale.json"),
            ).bufferedReader().use { it.readText() }

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
}
