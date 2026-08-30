package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import javax.inject.Inject

class UpdateTrackingCadenceUseCase @Inject constructor(
    private val repository: TrackingPreferencesRepository,
) {
    suspend operator fun invoke(cadence: TrackingCadence) {
        repository.setCadence(cadence)
    }
}
