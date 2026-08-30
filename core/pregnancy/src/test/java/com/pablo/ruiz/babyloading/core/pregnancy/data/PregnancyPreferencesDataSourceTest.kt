package com.pablo.ruiz.babyloading.core.pregnancy.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PregnancyPreferencesDataSourceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun dateRoundTripsAndCanBeCleared() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("pregnancy.preferences_pb") },
        )
        val dataSource = DataStorePregnancyPreferencesDataSource(dataStore)
        val date = LocalDate.of(2026, 5, 10)

        dataSource.setLastPeriodDate(date)
        assertEquals(date, dataSource.lastPeriodDate.first())

        dataSource.clearLastPeriodDate()
        assertNull(dataSource.lastPeriodDate.first())
    }

    @Test
    fun invalidIsoDateIsExposedAsMissingSetup() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("invalid-date.preferences_pb") },
        )
        val dataSource = DataStorePregnancyPreferencesDataSource(dataStore)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_period_date")] = "not-an-iso-date"
        }

        assertNull(dataSource.lastPeriodDate.first())
    }
}
