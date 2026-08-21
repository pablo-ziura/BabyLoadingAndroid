package com.pablo.ruiz.babyloading.app.lifecycle

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageChanges
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppLanguageForegroundRefreshViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun foregroundLanguageChangeRefreshesWidgets() = runTest {
        val languageChanges = FakeLanguageChanges(languageChanged = true)
        val widgetNotifier = RecordingWidgetNotifier()
        val viewModel = AppLanguageForegroundRefreshViewModel(languageChanges, widgetNotifier)

        viewModel.onAppForeground()
        advanceUntilIdle()

        assertEquals(1, languageChanges.refreshCalls)
        assertEquals(1, widgetNotifier.refreshCalls)
    }

    @Test
    fun unchangedForegroundLanguageDoesNotRefreshWidgets() = runTest {
        val languageChanges = FakeLanguageChanges(languageChanged = false)
        val widgetNotifier = RecordingWidgetNotifier()
        val viewModel = AppLanguageForegroundRefreshViewModel(languageChanges, widgetNotifier)

        viewModel.onAppForeground()
        advanceUntilIdle()

        assertEquals(1, languageChanges.refreshCalls)
        assertEquals(0, widgetNotifier.refreshCalls)
    }

    private class FakeLanguageChanges(
        private val languageChanged: Boolean,
    ) : AppLanguageChanges {
        var refreshCalls = 0
        override val changes: Flow<AppLanguage> = emptyFlow()

        override suspend fun refreshIfLanguageChanged(): Boolean {
            refreshCalls += 1
            return languageChanged
        }
    }

    private class RecordingWidgetNotifier : PregnancyDataChangeNotifier {
        var refreshCalls = 0

        override suspend fun onPregnancyDataChanged() {
            refreshCalls += 1
        }
    }
}
