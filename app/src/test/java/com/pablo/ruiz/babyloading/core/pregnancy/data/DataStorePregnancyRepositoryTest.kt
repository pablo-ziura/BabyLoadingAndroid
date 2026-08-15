package com.pablo.ruiz.babyloading.core.pregnancy.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import java.time.LocalDate
import java.io.IOException
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
        val changeNotifier = RecordingChangeNotifier()
        val repository = DataStorePregnancyRepository(dataStore, changeNotifier)
        val date = LocalDate.of(2026, 5, 10)

        repository.setLastPeriodDate(date)
        assertEquals(date, repository.lastPeriodDate.first())

        repository.clearLastPeriodDate()
        assertNull(repository.lastPeriodDate.first())
        assertEquals(2, changeNotifier.changeCount)
    }

    @Test
    fun widgetNotificationFailureDoesNotLoseSavedDate() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("notification-failure.preferences_pb") },
        )
        val repository = DataStorePregnancyRepository(
            dataStore = dataStore,
            changeNotifier = RecordingChangeNotifier(fail = true),
        )
        val date = LocalDate.of(2026, 5, 10)

        repository.setLastPeriodDate(date)

        assertEquals(date, repository.lastPeriodDate.first())
    }

    private class RecordingChangeNotifier(
        private val fail: Boolean = false,
    ) : PregnancyDataChangeNotifier {
        var changeCount = 0

        override suspend fun onPregnancyDataChanged() {
            changeCount += 1
            if (fail) throw IOException("Widget host unavailable")
        }
    }
}
