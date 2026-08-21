package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent

interface PregnancyContentRepository {
    fun contentForWeek(week: Int, language: AppLanguage): WeekContent?

    fun allContent(language: AppLanguage): List<WeekContent>
}
