package com.pablo.ruiz.babyloading.feature.gallery.domain.repository

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface GalleryRepository {
    val items: Flow<List<GalleryItem>>

    suspend fun importPhotos(sourceUris: List<String>): GalleryImportResult

    suspend fun addPrivatePhoto(
        data: ByteArray,
        source: GallerySource,
        capturedAt: Instant,
        pregnancyWeek: Int?,
    ): GalleryItem

    suspend fun deletePrivateCopy(id: String)
}
