package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: GalleryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.items
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = GalleryUserMessage.ImportFailed)
                    }
                }
                .collect { items ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            items = items,
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
            GalleryEvent.DeleteConfirmed -> deletePendingItem()
            GalleryEvent.DialogDismissed -> _uiState.update { state ->
                state.copy(selectedItem = null, pendingDeleteItem = null)
            }
            GalleryEvent.MessageShown -> _uiState.update { it.copy(message = null) }
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
}
