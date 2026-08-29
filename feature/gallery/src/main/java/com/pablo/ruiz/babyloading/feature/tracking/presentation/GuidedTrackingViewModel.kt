package com.pablo.ruiz.babyloading.feature.tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.ObserveGuidedTrackingContextUseCase
import com.pablo.ruiz.babyloading.feature.tracking.domain.usecase.SaveGuidedTrackingPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GuidedTrackingViewModel @Inject constructor(
    observeGuidedTrackingContext: ObserveGuidedTrackingContextUseCase,
    private val savePhoto: SaveGuidedTrackingPhotoUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuidedTrackingUiState())
    val uiState: StateFlow<GuidedTrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeGuidedTrackingContext().collect { context ->
                _uiState.update { state ->
                    state.copy(
                        pregnancyWeek = context.pregnancyWeek,
                        referenceImagePath = context.referenceImagePath,
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
        val pregnancyWeek = _uiState.value.pregnancyWeek
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val result = savePhoto(
                    data = data,
                    pregnancyWeek = pregnancyWeek,
                )
                _uiState.update {
                    it.copy(
                        saveOutcome = if (result.publicCopySaved) {
                            GuidedTrackingSaveOutcome.PrivateAndPublic
                        } else {
                            GuidedTrackingSaveOutcome.PrivateOnly
                        },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(error = GuidedTrackingError.SaveFailed)
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
