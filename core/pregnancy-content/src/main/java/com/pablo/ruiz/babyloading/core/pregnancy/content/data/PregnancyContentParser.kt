package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import javax.inject.Inject
import kotlinx.serialization.json.Json

class PregnancyContentParser @Inject constructor(
    @param:PregnancyContentJson private val json: Json,
) {
    fun parse(
        content: String,
        expectedLocale: String,
    ): PregnancyContentDocument {
        return json
            .decodeFromString<PregnancyContentDocument>(content)
            .validated(expectedLocale)
    }
}
