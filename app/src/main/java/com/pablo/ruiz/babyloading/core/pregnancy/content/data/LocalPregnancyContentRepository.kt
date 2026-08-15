package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.PregnancyContentLocaleResolver
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPregnancyContentRepository @Inject constructor(
    private val source: PregnancyContentSource,
    private val localeResolver: PregnancyContentLocaleResolver,
) : PregnancyContentRepository {
    private val cachedDocuments = ConcurrentHashMap<String, PregnancyContentDocument>()

    override fun contentForWeek(week: Int, locale: Locale): WeekContent? {
        if (week < PregnancyContentDocument.CoveredWeeks.first()) return null

        val contentWeek = week.coerceAtMost(PregnancyContentDocument.CoveredWeeks.last())
        return documentFor(locale)?.weeks?.firstOrNull { content -> content.week == contentWeek }
    }

    override fun allContent(locale: Locale): List<WeekContent> {
        return documentFor(locale)?.weeks.orEmpty()
    }

    private fun documentFor(locale: Locale): PregnancyContentDocument? {
        val localeCode = localeResolver.resolve(locale)
        cachedDocuments[localeCode]?.let { return it }

        return runCatching { source.load(localeCode) }
            .getOrNull()
            ?.also { document -> cachedDocuments[localeCode] = document }
    }
}
