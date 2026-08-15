package com.pablo.ruiz.babyloading.feature.gallery.domain.model

import java.time.Instant

data class GalleryItem(
    val id: String,
    val privateFilePath: String,
    val capturedAt: Instant,
    val source: GallerySource,
    val pregnancyWeek: Int? = null,
)

enum class GallerySource {
    Imported,
    GuidedTracking,
}

data class GalleryImportResult(
    val importedCount: Int,
    val failedCount: Int,
)
