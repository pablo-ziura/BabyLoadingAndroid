package com.pablo.ruiz.babyloading.feature.settings.presentation

import java.time.LocalDate

data class SettingsUiState(
    val isLoading: Boolean = true,
    val savedDate: LocalDate? = null,
    val selectedDate: LocalDate? = null,
    val minimumDate: LocalDate = LocalDate.MIN,
    val maximumDate: LocalDate = LocalDate.MIN,
    val estimatedDueDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val validationError: SettingsValidationError? = null,
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
