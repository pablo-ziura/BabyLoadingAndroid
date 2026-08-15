package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import java.util.Locale

interface PregnancyContentRepository {
    fun contentForWeek(week: Int, locale: Locale): WeekContent?

    fun allContent(locale: Locale): List<WeekContent>
}
