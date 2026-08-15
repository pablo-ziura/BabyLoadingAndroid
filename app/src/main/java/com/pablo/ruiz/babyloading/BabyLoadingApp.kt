package com.pablo.ruiz.babyloading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingViewModel
import com.pablo.ruiz.babyloading.navigation.AppNavigation
import com.pablo.ruiz.babyloading.navigation.MainShellGraph
import com.pablo.ruiz.babyloading.navigation.OnboardingRoute

@Composable
fun BabyLoadingApp(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> AppLoadingIndicator()
        else -> {
            val startDestination = if (uiState.isConfigured) MainShellGraph else OnboardingRoute
            key(startDestination) {
                AppNavigation(
                    startDestination = startDestination,
                    onboardingUiState = uiState,
                    onOnboardingEvent = viewModel::onEvent,
                )
            }
        }
    }
}

@Composable
private fun AppLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
