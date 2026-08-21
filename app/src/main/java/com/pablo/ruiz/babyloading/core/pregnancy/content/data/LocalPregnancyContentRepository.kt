package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPregnancyContentRepository @Inject constructor(
    private val source: PregnancyContentSource,
) : PregnancyContentRepository {
    private val cachedDocuments = ConcurrentHashMap<String, PregnancyContentDocument>()

    override fun contentForWeek(week: Int, language: AppLanguage): WeekContent? {
        if (week < PregnancyContentDocument.CoveredWeeks.first()) return null

        val contentWeek = week.coerceAtMost(PregnancyContentDocument.CoveredWeeks.last())
        return documentFor(language)?.weeks?.firstOrNull { content -> content.week == contentWeek }
    }

    override fun allContent(language: AppLanguage): List<WeekContent> {
        return documentFor(language)?.weeks.orEmpty()
    }

    private fun documentFor(language: AppLanguage): PregnancyContentDocument? {
        val localeCode = language.languageTag
        cachedDocuments[localeCode]?.let { return it }

        return runCatching { source.load(localeCode) }
            .getOrNull()
            ?.also { document -> cachedDocuments[localeCode] = document }
    }
}
