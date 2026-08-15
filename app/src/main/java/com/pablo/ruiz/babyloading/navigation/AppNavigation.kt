package com.pablo.ruiz.babyloading.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingScreen
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingEvent
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingUiState

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: Any = OnboardingRoute,
    onboardingUiState: OnboardingUiState = OnboardingUiState(isLoading = false),
    onOnboardingEvent: (OnboardingEvent) -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                uiState = onboardingUiState,
                onEvent = onOnboardingEvent,
            )
        }
        composable<MainShellGraph> {
            MainNavigation()
        }
    }
}
