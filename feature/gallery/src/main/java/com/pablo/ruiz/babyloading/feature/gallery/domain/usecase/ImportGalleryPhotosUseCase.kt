package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class ImportGalleryPhotosUseCase @Inject constructor(
    private val repository: GalleryRepository,
) {
    suspend operator fun invoke(sourceUris: List<String>): GalleryImportResult {
        val distinctUris = sourceUris.distinct()
        val selectedUris = distinctUris.take(MaximumPhotoSelection)
        val result = repository.importPhotos(selectedUris)
        return result.copy(
            failedCount = result.failedCount + distinctUris.size - selectedUris.size,
        )
    }

    companion object {
        const val MaximumPhotoSelection = 10
    }
}
