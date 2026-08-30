package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex

@Singleton
class LocalPregnancyContentRepository @Inject constructor(
    private val dataSource: PregnancyContentDataSource,
) : PregnancyContentRepository {
    private val cachedDocuments = ConcurrentHashMap<String, PregnancyContentDocument>()
    private val cacheMutex = Mutex()

    override suspend fun contentForWeek(week: Int, language: AppLanguage): WeekContent? {
        if (week !in PregnancyContentDocument.CoveredWeeks) return null

        return documentFor(language)?.weeks?.firstOrNull { content -> content.week == week }
    }

    override suspend fun allContent(language: AppLanguage): List<WeekContent> {
        return documentFor(language)?.weeks.orEmpty()
    }

    private suspend fun documentFor(language: AppLanguage): PregnancyContentDocument? {
        val localeCode = language.languageTag
        cachedDocuments[localeCode]?.let { return it }

        cacheMutex.lock()
        return try {
            cachedDocuments[localeCode] ?: try {
                dataSource.load(localeCode).also { document ->
                    cachedDocuments[localeCode] = document
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
        } finally {
            cacheMutex.unlock()
        }
    }
}
