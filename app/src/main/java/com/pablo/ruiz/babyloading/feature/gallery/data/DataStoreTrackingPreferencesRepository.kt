package com.pablo.ruiz.babyloading.feature.gallery.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreTrackingPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    storageConfig: AppStorageConfig,
) : TrackingPreferencesRepository {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = {
            context.preferencesDataStoreFile(storageConfig.trackingPreferences)
        },
    )

    override val cadence: Flow<TrackingCadence> = dataStore.data
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
        dataStore.edit { preferences ->
            preferences[TrackingCadenceDaysKey] = cadence.intervalDays
        }
    }

    private companion object {
        val TrackingCadenceDaysKey = intPreferencesKey("tracking_cadence_days")
    }
}
