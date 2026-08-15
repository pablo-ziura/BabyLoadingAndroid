package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.SavePregnancyDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: PregnancyRepository,
    private val savePregnancyDate: SavePregnancyDateUseCase,
    clock: Clock,
) : ViewModel() {
    private val currentDate = LocalDate.now(clock)
    private val _uiState = MutableStateFlow(OnboardingUiState(maximumDate = currentDate))
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.lastPeriodDate.collectLatest { storedDate ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        storedDate = storedDate,
                        selectedDate = state.selectedDate ?: storedDate,
                    )
                }
            }
        }
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.DateSelected -> selectDate(event.date)
            OnboardingEvent.Continue -> saveSelection()
        }
    }

    private fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                validationError = null,
            )
        }
    }

    private fun saveSelection() {
        val selectedDate = _uiState.value.selectedDate ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationError = null) }
            when (savePregnancyDate(selectedDate)) {
                PregnancyDateValidation.Valid -> Unit
                PregnancyDateValidation.FutureDate -> {
                    _uiState.update { state ->
                        state.copy(validationError = OnboardingValidationError.FutureDate)
                    }
                }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
