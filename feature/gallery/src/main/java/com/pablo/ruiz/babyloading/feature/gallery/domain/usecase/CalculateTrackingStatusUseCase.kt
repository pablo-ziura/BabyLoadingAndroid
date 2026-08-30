package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingStatus
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.trackingStatus
import java.time.Clock
import javax.inject.Inject

class CalculateTrackingStatusUseCase @Inject constructor(
    private val clock: Clock,
) {
    operator fun invoke(
        items: List<GalleryItem>,
        cadence: TrackingCadence,
    ): TrackingStatus {
        return cadence.trackingStatus(
            lastCapture = items
                .asSequence()
                .filter { it.source == GallerySource.GuidedTracking }
                .maxByOrNull(GalleryItem::capturedAt)
                ?.capturedAt,
            asOf = clock.instant(),
            zoneId = clock.zone,
        )
    }
}
