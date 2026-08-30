package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryData
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveGalleryDataUseCase @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val preferencesRepository: TrackingPreferencesRepository,
    private val calculateTrackingStatus: CalculateTrackingStatusUseCase,
) {
    operator fun invoke(): Flow<GalleryData> = combine(
        galleryRepository.items,
        preferencesRepository.cadence,
    ) { items, cadence ->
        GalleryData(
            items = items,
            cadence = cadence,
            trackingStatus = calculateTrackingStatus(items, cadence),
        )
    }
}
