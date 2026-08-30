package com.pablo.ruiz.babyloading.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.SavePregnancyDateUseCase
import com.pablo.ruiz.babyloading.feature.settings.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val savePregnancyDate: SavePregnancyDateUseCase,
    private val calculator: PregnancyCalculator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettings().collectLatest { data ->
                _uiState.update { state ->
                    val currentState = state.copy(
                        minimumDate = data.minimumDate,
                        maximumDate = data.maximumDate,
                        appLanguage = data.appLanguage,
                    )
                    when (data.pregnancyProgress) {
                        is PregnancyProgress.InvalidFutureLastPeriodDate -> currentState.copy(
                            isLoading = false,
                            savedDate = data.savedDate,
                            selectedDate = data.maximumDate,
                            estimatedDueDate = null,
                            hasStoredFutureDate = true,
                        )

                        is PregnancyProgress.Active -> {
                            val selectedDate = state.selectedDate ?: data.savedDate
                            currentState.copy(
                                isLoading = false,
                                savedDate = data.savedDate,
                                selectedDate = selectedDate,
                                estimatedDueDate = selectedDate?.let(calculator::estimatedDueDate),
                                hasStoredFutureDate = false,
                            )
                        }

                        null -> currentState.copy(
                            isLoading = false,
                            savedDate = null,
                            selectedDate = null,
                            estimatedDueDate = null,
                            hasStoredFutureDate = false,
                        )
                    }
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
        _uiState.update { it.copy(isSaving = true, validationError = null) }

        viewModelScope.launch {
            try {
                when (savePregnancyDate(selectedDate)) {
                    PregnancyDateValidation.Valid -> {
                        _uiState.update { it.copy(saveCompleted = true) }
                    }
                    PregnancyDateValidation.FutureDate -> {
                        _uiState.update {
                            it.copy(validationError = SettingsValidationError.FutureDate)
                        }
                    }
                    PregnancyDateValidation.DateTooOld -> {
                        _uiState.update {
                            it.copy(validationError = SettingsValidationError.DateTooOld)
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Keep the current UI state; existing validation and success copy remain authoritative.
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
