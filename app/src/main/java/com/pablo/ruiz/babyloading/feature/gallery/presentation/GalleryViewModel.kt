package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.trackingStatus
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: GalleryRepository,
    private val trackingPreferencesRepository: TrackingPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.importedItems,
                repository.trackingItems,
                trackingPreferencesRepository.cadence,
            ) { importedItems, trackingItems, cadence ->
                GalleryItemsAndCadence(importedItems, trackingItems, cadence)
            }
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = GalleryUserMessage.ImportFailed)
                    }
                }
                .collect { (importedItems, trackingItems, cadence) ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            importedItems = importedItems,
                            trackingItems = trackingItems,
                            trackingCadence = cadence,
                            trackingStatus = trackingStatus(trackingItems, cadence),
                            selectedItem = state.selectedItem?.let { item ->
                                itemById(item.id, importedItems, trackingItems)
                            },
                            pendingDeleteItem = state.pendingDeleteItem?.let { item ->
                                itemById(item.id, importedItems, trackingItems)
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
                state.copy(trackingStatus = trackingStatus(state.trackingItems, state.trackingCadence))
            }
        }
    }

    private fun updateTrackingCadence(cadence: TrackingCadence) {
        if (cadence == _uiState.value.trackingCadence) return
        viewModelScope.launch {
            runCatching { trackingPreferencesRepository.setCadence(cadence) }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(message = GalleryUserMessage.CadenceUpdateFailed)
                    }
                }
        }
    }

    private fun importPhotos(uriValues: List<String>) {
        if (uriValues.isEmpty() || _uiState.value.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val message = runCatching { repository.importPhotos(uriValues) }
                .fold(
                    onSuccess = { result ->
                        when {
                            result.failedCount == 0 -> GalleryUserMessage.ImportCompleted(result.importedCount)
                            result.importedCount > 0 -> GalleryUserMessage.ImportPartiallyCompleted(
                                importedCount = result.importedCount,
                                failedCount = result.failedCount,
                            )
                            else -> GalleryUserMessage.ImportFailed
                        }
                    },
                    onFailure = { GalleryUserMessage.ImportFailed },
                )
            _uiState.update { it.copy(isImporting = false, message = message) }
        }
    }

    private fun deletePendingItem() {
        val item = _uiState.value.pendingDeleteItem ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingDeleteItem = null, selectedItem = null) }
            runCatching { repository.deleteItem(item.id) }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(message = GalleryUserMessage.DeleteFailed)
                    }
                }
        }
    }

    private fun trackingStatus(
        trackingItems: List<GalleryItem>,
        cadence: TrackingCadence,
    ) = cadence.trackingStatus(
        lastCapture = trackingItems.maxByOrNull(GalleryItem::capturedAt)?.capturedAt,
        asOf = clock.instant(),
        zoneId = clock.zone,
    )

    private fun itemById(
        id: String,
        importedItems: List<GalleryItem>,
        trackingItems: List<GalleryItem>,
    ): GalleryItem? {
        return importedItems.firstOrNull { it.id == id }
            ?: trackingItems.firstOrNull { it.id == id }
    }

    private data class GalleryItemsAndCadence(
        val importedItems: List<GalleryItem>,
        val trackingItems: List<GalleryItem>,
        val cadence: TrackingCadence,
    )
}
