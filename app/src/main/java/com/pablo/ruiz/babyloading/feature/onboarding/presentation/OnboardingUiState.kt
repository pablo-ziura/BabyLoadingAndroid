package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import java.time.LocalDate

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val storedDate: LocalDate? = null,
    val selectedDate: LocalDate? = null,
    val maximumDate: LocalDate = LocalDate.MIN,
    val isSaving: Boolean = false,
    val validationError: OnboardingValidationError? = null,
) {
    val isConfigured: Boolean
        get() = !isLoading && storedDate != null

    val canContinue: Boolean
        get() = selectedDate != null && !isSaving
}

enum class OnboardingValidationError {
    FutureDate,
}

sealed interface OnboardingEvent {
    data class DateSelected(val date: LocalDate) : OnboardingEvent

    data object Continue : OnboardingEvent
}
