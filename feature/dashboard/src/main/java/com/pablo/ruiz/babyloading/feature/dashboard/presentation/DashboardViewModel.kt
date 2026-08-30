package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.feature.dashboard.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
) : ViewModel() {
    private val foregroundSignals = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val uiState: StateFlow<DashboardUiState> = observeDashboard(foregroundSignals)
        .map { data ->
            DashboardUiState(
                isLoading = false,
                progress = data.progress,
                weekContent = data.weekContent,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardUiState(),
        )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Refresh -> foregroundSignals.tryEmit(Unit)
        }
    }
}
