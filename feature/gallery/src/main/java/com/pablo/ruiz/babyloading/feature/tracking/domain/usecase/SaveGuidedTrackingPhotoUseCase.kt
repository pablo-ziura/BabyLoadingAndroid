package com.pablo.ruiz.babyloading.feature.tracking.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.tracking.domain.model.GuidedCaptureResult
import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class SaveGuidedTrackingPhotoUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val photoExporter: TrackingPhotoExporter,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        data: ByteArray,
        pregnancyWeek: Int?,
    ): GuidedCaptureResult {
        require(data.isNotEmpty()) { "Captured photo cannot be empty" }
        val capturedAt = clock.instant()
        val galleryItem = galleryRepository.addPrivatePhoto(
            data = data,
            source = GallerySource.GuidedTracking,
            capturedAt = capturedAt,
            pregnancyWeek = pregnancyWeek,
        )
        val publicCopySaved = try {
            photoExporter.exportJpeg(data, capturedAt)
            true
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            false
        }
        return GuidedCaptureResult(
            galleryItem = galleryItem,
            publicCopySaved = publicCopySaved,
        )
    }
}
