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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface TrackingPreferencesDataSource {
    val cadenceDays: Flow<Int>

    suspend fun setCadenceDays(days: Int)
}

@Singleton
class DataStoreTrackingPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    storageConfig: AppStorageConfig,
) : TrackingPreferencesDataSource {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = {
            context.preferencesDataStoreFile(storageConfig.trackingPreferences)
        },
    )

    override val cadenceDays: Flow<Int> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[TrackingCadenceDaysKey] ?: DEFAULT_CADENCE_DAYS
        }

    override suspend fun setCadenceDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[TrackingCadenceDaysKey] = days
        }
    }

    private companion object {
        val TrackingCadenceDaysKey = intPreferencesKey("tracking_cadence_days")
        const val DEFAULT_CADENCE_DAYS = 7
    }
}
