package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val items: List<GalleryItem> = emptyList(),
    val selectedItem: GalleryItem? = null,
    val pendingDeleteItem: GalleryItem? = null,
    val message: GalleryUserMessage? = null,
)

sealed interface GalleryUserMessage {
    data class ImportCompleted(val importedCount: Int) : GalleryUserMessage

    data class ImportPartiallyCompleted(
        val importedCount: Int,
        val failedCount: Int,
    ) : GalleryUserMessage

    data object ImportFailed : GalleryUserMessage

    data object DeleteFailed : GalleryUserMessage
}

sealed interface GalleryEvent {
    data class PhotosSelected(val uriValues: List<String>) : GalleryEvent

    data class ItemSelected(val id: String) : GalleryEvent

    data class DeleteRequested(val id: String) : GalleryEvent

    data object DeleteConfirmed : GalleryEvent

    data object DialogDismissed : GalleryEvent

    data object MessageShown : GalleryEvent
}
