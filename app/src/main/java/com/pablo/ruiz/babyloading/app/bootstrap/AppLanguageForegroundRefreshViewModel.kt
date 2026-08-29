package com.pablo.ruiz.babyloading.app.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.localization.AppLanguageRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppLanguageForegroundRefreshViewModel @Inject constructor(
    private val languageRepository: AppLanguageRepository,
    private val widgetNotifier: PregnancyDataChangeNotifier,
) : ViewModel() {
    fun onAppForeground() {
        viewModelScope.launch {
            languageRepository.refreshIfChanged()
            runCatching { widgetNotifier.onPregnancyDataChanged() }
        }
    }
}
