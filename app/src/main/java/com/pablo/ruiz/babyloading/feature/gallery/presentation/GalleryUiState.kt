package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import java.time.LocalDate

data class GalleryUiState(
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val items: List<GalleryItem> = emptyList(),
    val selectedItem: GalleryItem? = null,
    val pendingDeleteItem: GalleryItem? = null,
    val trackingCadence: TrackingCadence = TrackingCadence.Default,
    val nextTrackingPhotoDate: LocalDate? = null,
    val isTrackingDue: Boolean = false,
    val message: GalleryUserMessage? = null,
) {
    val trackingItems: List<GalleryItem>
        get() = items.filter { it.source == GallerySource.GuidedTracking }

    val importedItems: List<GalleryItem>
        get() = items.filter { it.source == GallerySource.Imported }

    val latestTrackingItem: GalleryItem?
        get() = trackingItems.maxByOrNull(GalleryItem::capturedAt)
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
}
