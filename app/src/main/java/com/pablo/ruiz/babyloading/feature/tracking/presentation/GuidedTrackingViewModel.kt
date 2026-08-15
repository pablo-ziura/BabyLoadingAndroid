package com.pablo.ruiz.babyloading.feature.tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.tracking.domain.usecase.SaveGuidedTrackingPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GuidedTrackingViewModel @Inject constructor(
    pregnancyRepository: PregnancyRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val savePhoto: SaveGuidedTrackingPhotoUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuidedTrackingUiState())
    val uiState: StateFlow<GuidedTrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pregnancyRepository.lastPeriodDate.collect { date ->
                _uiState.update { state ->
                    state.copy(
                        pregnancyWeek = date?.let { calculateProgress(it).gestationalAge.completedWeeks },
                    )
                }
            }
        }
    }

    fun onEvent(event: GuidedTrackingEvent) {
        when (event) {
            is GuidedTrackingEvent.PhotoCaptured -> save(event.data)
            GuidedTrackingEvent.CaptureFailed -> _uiState.update {
                it.copy(error = GuidedTrackingError.CaptureFailed)
            }
            GuidedTrackingEvent.ErrorShown -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun save(data: ByteArray) {
        if (_uiState.value.isSaving || _uiState.value.saveOutcome != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                savePhoto(
                    data = data,
                    pregnancyWeek = _uiState.value.pregnancyWeek,
                )
            }.fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveOutcome = if (result.publicCopySaved) {
                                GuidedTrackingSaveOutcome.PrivateAndPublic
                            } else {
                                GuidedTrackingSaveOutcome.PrivateOnly
                            },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = GuidedTrackingError.SaveFailed,
                        )
                    }
                },
            )
        }
    }
}
