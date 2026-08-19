package com.pablo.ruiz.babyloading.feature.gallery.domain.repository

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import kotlinx.coroutines.flow.Flow

interface TrackingPreferencesRepository {
    val cadence: Flow<TrackingCadence>

    suspend fun setCadence(cadence: TrackingCadence)
}
