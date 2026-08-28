package com.pablo.ruiz.babyloading.feature.settings.domain.model

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import java.time.LocalDate

data class SettingsData(
    val savedDate: LocalDate? = null,
    val pregnancyProgress: PregnancyProgress? = null,
    val minimumDate: LocalDate,
    val maximumDate: LocalDate,
    val appLanguage: AppLanguage,
)
