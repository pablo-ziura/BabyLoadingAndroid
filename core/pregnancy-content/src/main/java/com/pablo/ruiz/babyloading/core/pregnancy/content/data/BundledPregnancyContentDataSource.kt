package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import android.content.Context
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class BundledPregnancyContentDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val parser: PregnancyContentParser,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PregnancyContentDataSource {
    override suspend fun load(localeCode: String): PregnancyContentDocument = withContext(ioDispatcher) {
        val content = context.assets
            .open("pregnancy-content.$localeCode.json")
            .bufferedReader()
            .use { reader -> reader.readText() }
        parser.parse(
            content = content,
            expectedLocale = localeCode,
        )
    }
}
