package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTrackingPreferencesRepositoryTest {
    @Test
    fun invalidStoredCadenceFallsBackToSevenDays() = runTest {
        val dataSource = FakeTrackingPreferencesDataSource(initialDays = 13)
        val repository = DefaultTrackingPreferencesRepository(dataSource)

        assertEquals(TrackingCadence.Weekly, repository.cadence.first())

        repository.setCadence(TrackingCadence.EveryFourWeeks)
        assertEquals(28, dataSource.days.value)
    }

    private class FakeTrackingPreferencesDataSource(initialDays: Int) :
        TrackingPreferencesDataSource {
        val days = MutableStateFlow(initialDays)
        override val cadenceDays: Flow<Int> = days

        override suspend fun setCadenceDays(days: Int) {
            this.days.value = days
        }
    }
}
