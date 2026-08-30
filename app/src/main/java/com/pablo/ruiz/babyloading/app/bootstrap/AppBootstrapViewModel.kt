package com.pablo.ruiz.babyloading.app.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.ObservePregnancySetupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AppBootstrapUiState(
    val isLoading: Boolean = true,
    val isPregnancyConfigured: Boolean = false,
)

@HiltViewModel
class AppBootstrapViewModel @Inject constructor(
    observePregnancySetup: ObservePregnancySetupUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AppBootstrapUiState())
    val uiState: StateFlow<AppBootstrapUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePregnancySetup().collectLatest { lastPeriodDate ->
                mutableUiState.value = AppBootstrapUiState(
                    isLoading = false,
                    isPregnancyConfigured = lastPeriodDate != null,
                )
            }
        }
    }
}
