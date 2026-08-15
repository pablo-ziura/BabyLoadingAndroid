package com.pablo.ruiz.babyloading.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
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
class SettingsViewModel @Inject constructor(
    private val repository: PregnancyRepository,
    private val savePregnancyDate: SavePregnancyDateUseCase,
    private val calculator: PregnancyCalculator,
    clock: Clock,
) : ViewModel() {
    private val currentDate = LocalDate.now(clock)
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            minimumDate = currentDate.minusWeeks(PregnancyDateValidator.MaximumPastWeeks.toLong()),
            maximumDate = currentDate,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.lastPeriodDate.collectLatest { savedDate ->
                _uiState.update { state ->
                    val selectedDate = state.selectedDate ?: savedDate
                    state.copy(
                        isLoading = false,
                        savedDate = savedDate,
                        selectedDate = selectedDate,
                        estimatedDueDate = selectedDate?.let(calculator::estimatedDueDate),
                    )
                }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.DateSelected -> selectDate(event.date)
            SettingsEvent.SaveDate -> saveDate()
            SettingsEvent.SaveMessageShown -> _uiState.update { it.copy(saveCompleted = false) }
        }
    }

    private fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                estimatedDueDate = calculator.estimatedDueDate(date),
                saveCompleted = false,
                validationError = null,
            )
        }
    }

    private fun saveDate() {
        val selectedDate = _uiState.value.selectedDate ?: return
        if (!_uiState.value.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationError = null) }
            when (savePregnancyDate(selectedDate)) {
                PregnancyDateValidation.Valid -> {
                    _uiState.update { it.copy(saveCompleted = true) }
                }
                PregnancyDateValidation.FutureDate -> {
                    _uiState.update { it.copy(validationError = SettingsValidationError.FutureDate) }
                }
                PregnancyDateValidation.DateTooOld -> {
                    _uiState.update { it.copy(validationError = SettingsValidationError.DateTooOld) }
                }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
