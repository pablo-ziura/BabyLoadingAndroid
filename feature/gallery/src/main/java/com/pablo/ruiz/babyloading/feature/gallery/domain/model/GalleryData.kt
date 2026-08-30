package com.pablo.ruiz.babyloading.feature.gallery.domain.model

data class GalleryData(
    val items: List<GalleryItem>,
    val cadence: TrackingCadence,
    val trackingStatus: TrackingStatus,
) {
    val importedItems: List<GalleryItem>
        get() = items.filter { it.source == GallerySource.Imported }

    val trackingItems: List<GalleryItem>
        get() = items.filter { it.source == GallerySource.GuidedTracking }
}

data class GuidedTrackingContext(
    val pregnancyWeek: Int? = null,
    val referenceImagePath: String? = null,
)
