package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultTrackingPreferencesRepository @Inject constructor(
    private val dataSource: TrackingPreferencesDataSource,
) : TrackingPreferencesRepository {
    override val cadence: Flow<TrackingCadence> = dataSource.cadenceDays.map(
        TrackingCadence::fromIntervalDays,
    )

    override suspend fun setCadence(cadence: TrackingCadence) {
        dataSource.setCadenceDays(cadence.intervalDays)
    }
}
