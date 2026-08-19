package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
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
            combine(repository.items, trackingPreferencesRepository.cadence) { items, cadence ->
                items to cadence
            }
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = GalleryUserMessage.ImportFailed)
                    }
                }
                .collect { (items, cadence) ->
                    val nextPhotoDate = calculateNextTrackingPhotoDate(items, cadence)
                    val today = LocalDate.now(clock)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            items = items,
                            trackingCadence = cadence,
                            nextTrackingPhotoDate = nextPhotoDate,
                            isTrackingDue = nextPhotoDate?.let { !it.isAfter(today) } == true,
                            selectedItem = items.firstOrNull { it.id == state.selectedItem?.id },
                            pendingDeleteItem = items.firstOrNull { it.id == state.pendingDeleteItem?.id },
                        )
                    }
                }
        }
    }

    fun onEvent(event: GalleryEvent) {
        when (event) {
            is GalleryEvent.PhotosSelected -> importPhotos(event.uriValues)
            is GalleryEvent.ItemSelected -> _uiState.update { state ->
                state.copy(selectedItem = state.items.firstOrNull { it.id == event.id })
            }
            is GalleryEvent.DeleteRequested -> _uiState.update { state ->
                state.copy(pendingDeleteItem = state.items.firstOrNull { it.id == event.id })
            }
            is GalleryEvent.TrackingCadenceSelected -> updateTrackingCadence(event.cadence)
            GalleryEvent.DeleteConfirmed -> deletePendingItem()
            GalleryEvent.DialogDismissed -> _uiState.update { state ->
                state.copy(selectedItem = null, pendingDeleteItem = null)
            }
            GalleryEvent.MessageShown -> _uiState.update { it.copy(message = null) }
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

    private fun calculateNextTrackingPhotoDate(
        items: List<GalleryItem>,
        cadence: TrackingCadence,
    ): LocalDate? {
        return items
            .asSequence()
            .filter { it.source == GallerySource.GuidedTracking }
            .maxByOrNull { it.capturedAt }
            ?.capturedAt
            ?.atZone(clock.zone)
            ?.toLocalDate()
            ?.plusDays(cadence.intervalDays.toLong())
    }
}
