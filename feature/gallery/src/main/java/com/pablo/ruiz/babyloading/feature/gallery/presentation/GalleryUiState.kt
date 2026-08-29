package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingStatus

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val importedItems: List<GalleryItem> = emptyList(),
    val trackingItems: List<GalleryItem> = emptyList(),
    val selectedItem: GalleryItem? = null,
    val pendingDeleteItem: GalleryItem? = null,
    val trackingCadence: TrackingCadence = TrackingCadence.Default,
    val trackingStatus: TrackingStatus = TrackingStatus.NeedsInitialCapture,
    val message: GalleryUserMessage? = null,
) {
    val latestTrackingItem: GalleryItem?
        get() = trackingItems.maxByOrNull(GalleryItem::capturedAt)

    fun itemById(id: String): GalleryItem? {
        return importedItems.firstOrNull { it.id == id }
            ?: trackingItems.firstOrNull { it.id == id }
    }
}

sealed interface GalleryUserMessage {
    data class ImportCompleted(val importedCount: Int) : GalleryUserMessage

    data class ImportPartiallyCompleted(
        val importedCount: Int,
        val failedCount: Int,
    ) : GalleryUserMessage

    data object ImportFailed : GalleryUserMessage

    data object DeleteFailed : GalleryUserMessage

    data object CadenceUpdateFailed : GalleryUserMessage
}

sealed interface GalleryEvent {
    data class PhotosSelected(val uriValues: List<String>) : GalleryEvent

    data class ItemSelected(val id: String) : GalleryEvent

    data class DeleteRequested(val id: String) : GalleryEvent

    data class TrackingCadenceSelected(val cadence: TrackingCadence) : GalleryEvent

    data object DeleteConfirmed : GalleryEvent

    data object DialogDismissed : GalleryEvent

    data object MessageShown : GalleryEvent

    data object TrackingStatusRefreshRequested : GalleryEvent
}
