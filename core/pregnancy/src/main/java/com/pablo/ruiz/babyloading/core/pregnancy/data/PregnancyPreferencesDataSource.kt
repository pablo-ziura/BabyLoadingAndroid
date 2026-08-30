package com.pablo.ruiz.babyloading.core.pregnancy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface PregnancyPreferencesDataSource {
    val lastPeriodDate: Flow<LocalDate?>

    suspend fun setLastPeriodDate(date: LocalDate)

    suspend fun clearLastPeriodDate()
}

@Singleton
class DataStorePregnancyPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PregnancyPreferencesDataSource {
    override val lastPeriodDate: Flow<LocalDate?> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[LastPeriodDateKey]?.let { value ->
                runCatching { LocalDate.parse(value) }.getOrNull()
            }
        }

    override suspend fun setLastPeriodDate(date: LocalDate) {
        dataStore.edit { preferences ->
            preferences[LastPeriodDateKey] = date.toString()
        }
    }

    override suspend fun clearLastPeriodDate() {
        dataStore.edit { preferences ->
            preferences.remove(LastPeriodDateKey)
        }
    }

    private companion object {
        val LastPeriodDateKey = stringPreferencesKey("last_period_date")
    }
}

@Singleton
class DefaultPregnancyRepository @Inject constructor(
    private val preferencesDataSource: PregnancyPreferencesDataSource,
) : PregnancyRepository {
    override val lastPeriodDate: Flow<LocalDate?> = preferencesDataSource.lastPeriodDate

    override suspend fun setLastPeriodDate(date: LocalDate) {
        preferencesDataSource.setLastPeriodDate(date)
    }

    override suspend fun clearLastPeriodDate() {
        preferencesDataSource.clearLastPeriodDate()
    }
}
