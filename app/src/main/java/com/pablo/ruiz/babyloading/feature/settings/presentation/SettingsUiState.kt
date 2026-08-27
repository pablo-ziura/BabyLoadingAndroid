package com.pablo.ruiz.babyloading.feature.settings.presentation

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import java.time.LocalDate

data class SettingsUiState(
    val isLoading: Boolean = true,
    val savedDate: LocalDate? = null,
    val selectedDate: LocalDate? = null,
    val minimumDate: LocalDate = LocalDate.MIN,
    val maximumDate: LocalDate = LocalDate.MIN,
    val estimatedDueDate: LocalDate? = null,
    val hasStoredFutureDate: Boolean = false,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val validationError: SettingsValidationError? = null,
    val appLanguage: AppLanguage = AppLanguage.English,
) {
    val hasChanges: Boolean
        get() = selectedDate != null && selectedDate != savedDate

    val canSave: Boolean
        get() = selectedDate != null && !isSaving
}

enum class SettingsValidationError {
    FutureDate,
    DateTooOld,
}

sealed interface SettingsEvent {
    data class DateSelected(val date: LocalDate) : SettingsEvent

    data object SaveDate : SettingsEvent

    data object SaveMessageShown : SettingsEvent
}
