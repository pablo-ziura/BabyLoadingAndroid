package com.pablo.ruiz.babyloading.app.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.localization.AppLanguageChanges
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppLanguageForegroundRefreshViewModel @Inject constructor(
    private val languageChanges: AppLanguageChanges,
    private val widgetNotifier: PregnancyDataChangeNotifier,
) : ViewModel() {
    fun onAppForeground() {
        viewModelScope.launch {
            languageChanges.refreshIfLanguageChanged()
            runCatching { widgetNotifier.onPregnancyDataChanged() }
        }
    }
}
