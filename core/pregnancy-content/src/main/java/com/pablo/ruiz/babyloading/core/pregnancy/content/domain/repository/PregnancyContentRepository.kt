package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent

interface PregnancyContentRepository {
    suspend fun contentForWeek(week: Int, language: AppLanguage): WeekContent?

    suspend fun allContent(language: AppLanguage): List<WeekContent>
}
