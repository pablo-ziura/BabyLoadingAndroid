package com.pablo.ruiz.babyloading.app.lifecycle

import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
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
        val languageRepository = FakeLanguageRepository(languageChanged = true)
        val widgetNotifier = RecordingWidgetNotifier()
        val viewModel = AppLanguageForegroundRefreshViewModel(languageRepository, widgetNotifier)

        viewModel.onAppForeground()
        advanceUntilIdle()

        assertEquals(1, languageRepository.refreshCalls)
        assertEquals(1, widgetNotifier.refreshCalls)
    }

    @Test
    fun unchangedForegroundLanguageRefreshesWidgets() = runTest {
        val languageRepository = FakeLanguageRepository(languageChanged = false)
        val widgetNotifier = RecordingWidgetNotifier()
        val viewModel = AppLanguageForegroundRefreshViewModel(languageRepository, widgetNotifier)

        viewModel.onAppForeground()
        advanceUntilIdle()

        assertEquals(1, languageRepository.refreshCalls)
        assertEquals(1, widgetNotifier.refreshCalls)
    }

    private class FakeLanguageRepository(
        private val languageChanged: Boolean,
    ) : AppLanguageRepository {
        var refreshCalls = 0
        override val changes: Flow<AppLanguage> = emptyFlow()

        override fun currentLanguage(): AppLanguage = AppLanguage.English

        override suspend fun refreshIfChanged(): Boolean {
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
