package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GuidedTrackingContext
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveGuidedTrackingContextUseCase @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val galleryRepository: GalleryRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
) {
    operator fun invoke(): Flow<GuidedTrackingContext> = combine(
        pregnancyRepository.lastPeriodDate,
        galleryRepository.items,
    ) { lastPeriodDate, items ->
        GuidedTrackingContext(
            pregnancyWeek = (lastPeriodDate
                ?.let(calculateProgress::invoke) as? PregnancyProgress.Active)
                ?.progress
                ?.gestationalAge
                ?.completedWeeks,
            referenceImagePath = items
                .asSequence()
                .filter { it.source == GallerySource.GuidedTracking }
                .maxByOrNull(GalleryItem::capturedAt)
                ?.privateFilePath,
        )
    }
}
