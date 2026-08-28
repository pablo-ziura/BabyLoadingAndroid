package com.pablo.ruiz.babyloading.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingDestination

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: Any = OnboardingRoute,
    onboardingContent: @Composable () -> Unit = { OnboardingDestination() },
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<OnboardingRoute> {
            onboardingContent()
        }
        composable<MainShellGraph> {
            MainNavigation()
        }
    }
}
