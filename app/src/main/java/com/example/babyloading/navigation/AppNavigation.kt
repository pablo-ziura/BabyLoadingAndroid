package com.example.babyloading.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyloading.feature.onboarding.presentation.OnboardingScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute,
        modifier = modifier
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(MainShellGraph) {
                        popUpTo<OnboardingRoute> {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<MainShellGraph> {
            MainNavigation()
        }
    }
}
