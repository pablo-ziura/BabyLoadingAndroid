package com.pablo.ruiz.babyloading.feature.tracking.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveGuidedTrackingPhotoUseCaseTest {
    private val capturedAt = Instant.parse("2026-08-15T12:00:00Z")
    private val galleryRepository = RecordingGalleryRepository()
    private val exporter = RecordingExporter()
    private val useCase = SaveGuidedTrackingPhotoUseCase(
        galleryRepository = galleryRepository,
        photoExporter = exporter,
        clock = Clock.fixed(capturedAt, ZoneOffset.UTC),
    )

    @Test
    fun savesPrivateTrackingMetadataBeforeExportingPublicCopy() = runTest {
        val data = byteArrayOf(1, 2, 3)

        val result = useCase(data = data, pregnancyWeek = 24)

        assertEquals(GallerySource.GuidedTracking, galleryRepository.savedSource)
        assertEquals(24, galleryRepository.savedWeek)
        assertEquals(capturedAt, galleryRepository.savedAt)
        assertArrayEquals(data, galleryRepository.savedData)
        assertArrayEquals(data, exporter.exportedData)
        assertEquals(capturedAt, exporter.exportedAt)
        assertTrue(result.publicCopySaved)
    }

    @Test
    fun preservesPrivateResultWhenMediaStoreExportFails() = runTest {
        exporter.failure = IOException("MediaStore unavailable")

        val result = useCase(data = byteArrayOf(1), pregnancyWeek = 24)

        assertFalse(result.publicCopySaved)
        assertEquals("saved", result.galleryItem.id)
    }

    private class RecordingGalleryRepository : GalleryRepository {
        override val items: Flow<List<GalleryItem>> = emptyFlow()
        var savedData: ByteArray? = null
        var savedSource: GallerySource? = null
        var savedAt: Instant? = null
        var savedWeek: Int? = null

        override suspend fun importPhotos(sourceUris: List<String>) = GalleryImportResult(0, 0)

        override suspend fun addPrivatePhoto(
            data: ByteArray,
            source: GallerySource,
            capturedAt: Instant,
            pregnancyWeek: Int?,
        ): GalleryItem {
            savedData = data
            savedSource = source
            savedAt = capturedAt
            savedWeek = pregnancyWeek
            return GalleryItem(
                id = "saved",
                privateFilePath = "/private/saved.jpg",
                capturedAt = capturedAt,
                source = source,
                pregnancyWeek = pregnancyWeek,
            )
        }

        override suspend fun deleteItem(id: String) = Unit
    }

    private class RecordingExporter : TrackingPhotoExporter {
        var exportedData: ByteArray? = null
        var exportedAt: Instant? = null
        var failure: Throwable? = null

        override suspend fun exportJpeg(data: ByteArray, capturedAt: Instant): String {
            failure?.let { throw it }
            exportedData = data
            exportedAt = capturedAt
            return "content://media/photo"
        }
    }
}
