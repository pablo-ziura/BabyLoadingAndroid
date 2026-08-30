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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.app.bootstrap.AppBootstrapViewModel
import com.pablo.ruiz.babyloading.app.bootstrap.AppLanguageForegroundRefreshViewModel
import com.pablo.ruiz.babyloading.navigation.AppNavigation
import com.pablo.ruiz.babyloading.navigation.MainShellGraph
import com.pablo.ruiz.babyloading.navigation.OnboardingRoute

@Composable
fun BabyLoadingApp(
    bootstrapViewModel: AppBootstrapViewModel = hiltViewModel(),
    languageForegroundRefreshViewModel: AppLanguageForegroundRefreshViewModel = hiltViewModel(),
) {
    val bootstrapUiState by bootstrapViewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        languageForegroundRefreshViewModel.onAppForeground()
    }

    when {
        bootstrapUiState.isLoading -> AppLoadingIndicator()
        else -> {
            val startDestination = if (bootstrapUiState.isPregnancyConfigured) {
                MainShellGraph
            } else {
                OnboardingRoute
            }
            key(startDestination) {
                AppNavigation(
                    startDestination = startDestination,
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
