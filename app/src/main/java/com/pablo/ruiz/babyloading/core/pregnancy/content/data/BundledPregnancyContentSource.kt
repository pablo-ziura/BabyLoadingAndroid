package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import android.content.Context
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledPregnancyContentSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val parser: PregnancyContentParser,
) : PregnancyContentSource {
    override fun load(localeCode: String): PregnancyContentDocument {
        val fileName = "pregnancy-content.$localeCode.json"
        val content = context.assets.open(fileName).bufferedReader().use { reader ->
            reader.readText()
        }
        return parser.parse(
            content = content,
            expectedLocale = localeCode,
        )
    }
}
