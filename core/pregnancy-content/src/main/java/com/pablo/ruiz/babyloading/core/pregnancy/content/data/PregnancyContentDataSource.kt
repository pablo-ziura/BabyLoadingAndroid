package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument

interface PregnancyContentDataSource {
    suspend fun load(localeCode: String): PregnancyContentDocument
}
