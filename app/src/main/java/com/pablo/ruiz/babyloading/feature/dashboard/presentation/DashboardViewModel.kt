package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.localization.AppLanguageChanges
import com.pablo.ruiz.babyloading.core.localization.AppLanguageProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val pregnancyRepository: PregnancyRepository,
    private val contentRepository: PregnancyContentRepository,
    private val calculateProgress: CalculatePregnancyProgressUseCase,
    private val languageProvider: AppLanguageProvider,
    private val languageChanges: AppLanguageChanges,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var lastPeriodDate: LocalDate? = null

    init {
        viewModelScope.launch {
            pregnancyRepository.lastPeriodDate.collectLatest { date ->
                lastPeriodDate = date
                load(date)
            }
        }
        viewModelScope.launch {
            languageChanges.changes.collectLatest {
                load(lastPeriodDate)
            }
        }
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Refresh -> viewModelScope.launch { load(lastPeriodDate) }
        }
    }

    private suspend fun load(date: LocalDate?) {
        if (date == null) {
            _uiState.value = DashboardUiState(isLoading = false)
            return
        }

        val state = withContext(ioDispatcher) {
            val progress = calculateProgress(date)
            val activeProgress = (progress as? PregnancyProgress.Active)?.progress
            DashboardUiState(
                isLoading = false,
                progress = progress,
                weekContent = activeProgress
                    ?.takeIf { active -> active.phase == PregnancyPhase.Ongoing }
                    ?.let { active ->
                        contentRepository.contentForWeek(
                            week = active.gestationalAge.completedWeeks,
                            language = languageProvider.currentLanguage(),
                        )
                    },
            )
        }
        _uiState.value = state
    }
}
