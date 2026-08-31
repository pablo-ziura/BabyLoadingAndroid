package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportGalleryPhotosUseCaseTest {
    @Test
    fun deduplicatesAndLimitsEachSelectionToTenPhotos() = runTest {
        val repository = RecordingGalleryRepository()
        val useCase = ImportGalleryPhotosUseCase(repository)
        val uris = (1..12).map { "content://photo/$it" } + "content://photo/1"

        val result = useCase(uris)

        assertEquals((1..10).map { "content://photo/$it" }, repository.importedUris)
        assertEquals(GalleryImportResult(importedCount = 10, failedCount = 2), result)
    }

    private class RecordingGalleryRepository : GalleryRepository {
        override val items: Flow<List<GalleryItem>> = emptyFlow()
        var importedUris: List<String> = emptyList()

        override suspend fun importPhotos(sourceUris: List<String>): GalleryImportResult {
            importedUris = sourceUris
            return GalleryImportResult(sourceUris.size, 0)
        }

        override suspend fun addPrivatePhotoFromFile(
            temporaryFilePath: String,
            source: GallerySource,
            capturedAt: Instant,
            pregnancyWeek: Int?,
        ): GalleryItem = error("Not used")

        override suspend fun deletePrivateCopy(id: String) = Unit
    }
}
