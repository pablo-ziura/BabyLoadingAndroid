package com.pablo.ruiz.babyloading.core.pregnancy.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePregnancyRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun dateRoundTripsAndCanBeCleared() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("pregnancy.preferences_pb") },
        )
        val repository = DataStorePregnancyRepository(dataStore)
        val date = LocalDate.of(2026, 5, 10)

        repository.setLastPeriodDate(date)
        assertEquals(date, repository.lastPeriodDate.first())

        repository.clearLastPeriodDate()
        assertNull(repository.lastPeriodDate.first())
    }
}
