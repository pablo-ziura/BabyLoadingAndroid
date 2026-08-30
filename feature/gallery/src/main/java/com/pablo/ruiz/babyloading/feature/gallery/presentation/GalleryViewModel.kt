package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.CalculateTrackingStatusUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.DeletePrivateGalleryCopyUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.ImportGalleryPhotosUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.ObserveGalleryDataUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.usecase.UpdateTrackingCadenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GalleryViewModel @Inject constructor(
    observeGalleryData: ObserveGalleryDataUseCase,
    private val importGalleryPhotos: ImportGalleryPhotosUseCase,
    private val deletePrivateGalleryCopy: DeletePrivateGalleryCopyUseCase,
    private val updateCadence: UpdateTrackingCadenceUseCase,
    private val calculateTrackingStatus: CalculateTrackingStatusUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeGalleryData()
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = GalleryUserMessage.ImportFailed)
                    }
                }
                .collect { data ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            importedItems = data.importedItems,
                            trackingItems = data.trackingItems,
                            trackingCadence = data.cadence,
                            trackingStatus = data.trackingStatus,
                            selectedItem = state.selectedItem?.let { item ->
                                data.items.firstOrNull { it.id == item.id }
                            },
                            pendingDeleteItem = state.pendingDeleteItem?.let { item ->
                                data.items.firstOrNull { it.id == item.id }
                            },
                        )
                    }
                }
        }
    }

    fun onEvent(event: GalleryEvent) {
        when (event) {
            is GalleryEvent.PhotosSelected -> importPhotos(event.uriValues)
            is GalleryEvent.ItemSelected -> _uiState.update { state ->
                state.copy(selectedItem = state.itemById(event.id))
            }
            is GalleryEvent.DeleteRequested -> _uiState.update { state ->
                state.copy(pendingDeleteItem = state.itemById(event.id))
            }
            is GalleryEvent.TrackingCadenceSelected -> updateTrackingCadence(event.cadence)
            GalleryEvent.DeleteConfirmed -> deletePendingItem()
            GalleryEvent.DialogDismissed -> _uiState.update { state ->
                state.copy(selectedItem = null, pendingDeleteItem = null)
            }
            GalleryEvent.MessageShown -> _uiState.update { it.copy(message = null) }
            GalleryEvent.TrackingStatusRefreshRequested -> _uiState.update { state ->
                state.copy(
                    trackingStatus = calculateTrackingStatus(
                        state.importedItems + state.trackingItems,
                        state.trackingCadence,
                    ),
                )
            }
        }
    }

    private fun updateTrackingCadence(cadence: TrackingCadence) {
        if (cadence == _uiState.value.trackingCadence) return
        viewModelScope.launch {
            try {
                updateCadence(cadence)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { state ->
                    state.copy(message = GalleryUserMessage.CadenceUpdateFailed)
                }
            }
        }
    }

    private fun importPhotos(uriValues: List<String>) {
        if (uriValues.isEmpty() || _uiState.value.isImporting) return
        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            var message: GalleryUserMessage = GalleryUserMessage.ImportFailed
            try {
                val result = importGalleryPhotos(uriValues)
                message = when {
                    result.failedCount == 0 -> {
                        GalleryUserMessage.ImportCompleted(result.importedCount)
                    }
                    result.importedCount > 0 -> GalleryUserMessage.ImportPartiallyCompleted(
                        importedCount = result.importedCount,
                        failedCount = result.failedCount,
                    )
                    else -> GalleryUserMessage.ImportFailed
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Existing failure copy is reused below.
            } finally {
                _uiState.update { it.copy(isImporting = false, message = message) }
            }
        }
    }

    private fun deletePendingItem() {
        val item = _uiState.value.pendingDeleteItem ?: return
        _uiState.update { it.copy(pendingDeleteItem = null, selectedItem = null) }
        viewModelScope.launch {
            try {
                deletePrivateGalleryCopy(item.id)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { state ->
                    state.copy(message = GalleryUserMessage.DeleteFailed)
                }
            }
        }
    }
}
