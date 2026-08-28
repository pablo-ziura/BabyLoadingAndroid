package com.pablo.ruiz.babyloading.feature.gallery.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pablo.ruiz.babyloading.core.storage.AppStorageNames
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.trackingPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppStorageNames.current.trackingPreferences,
)

@Singleton
class DataStoreTrackingPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TrackingPreferencesRepository {
    override val cadence: Flow<TrackingCadence> = context.trackingPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            TrackingCadence.fromIntervalDays(
                preferences[TrackingCadenceDaysKey] ?: TrackingCadence.Default.intervalDays,
            )
        }

    override suspend fun setCadence(cadence: TrackingCadence) {
        context.trackingPreferencesDataStore.edit { preferences ->
            preferences[TrackingCadenceDaysKey] = cadence.intervalDays
        }
    }

    private companion object {
        val TrackingCadenceDaysKey = intPreferencesKey("tracking_cadence_days")
    }
}
