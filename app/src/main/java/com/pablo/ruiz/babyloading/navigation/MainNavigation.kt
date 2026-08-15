package com.pablo.ruiz.babyloading.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.pablo.ruiz.babyloading.app.shell.MainScreen
import com.pablo.ruiz.babyloading.app.shell.MainTab
import com.pablo.ruiz.babyloading.feature.dashboard.presentation.DashboardScreen
import com.pablo.ruiz.babyloading.feature.gallery.presentation.GalleryScreen
import com.pablo.ruiz.babyloading.feature.journey.presentation.JourneyScreen
import com.pablo.ruiz.babyloading.feature.settings.presentation.SettingsScreen

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = MainTab.entries.firstOrNull { tab ->
        navBackStackEntry?.destination.isInGraph(tab)
    } ?: MainTab.Dashboard

    MainScreen(
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = { tab ->
            navController.navigate(tab.graphRoute, tabNavigationOptions(navController))
        }
    ) { innerPadding ->
        TabNavigationHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun TabNavigationHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardGraph,
        modifier = modifier
    ) {
        navigation<DashboardGraph>(startDestination = DashboardRoute) {
            composable<DashboardRoute> {
                DashboardScreen()
            }
        }
        navigation<JourneyGraph>(startDestination = JourneyRoute) {
            composable<JourneyRoute> {
                JourneyScreen()
            }
        }
        navigation<GalleryGraph>(startDestination = GalleryRoute) {
            composable<GalleryRoute> {
                GalleryScreen()
            }
        }
        navigation<SettingsGraph>(startDestination = SettingsRoute) {
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}

private fun tabNavigationOptions(navController: NavHostController) = navOptions {
    launchSingleTop = true
    restoreState = true
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
}

private fun NavDestination?.isInGraph(tab: MainTab): Boolean = when (tab) {
    MainTab.Dashboard -> hasRoute(DashboardGraph::class.qualifiedName.orEmpty(), null)
    MainTab.Journey -> hasRoute(JourneyGraph::class.qualifiedName.orEmpty(), null)
    MainTab.Gallery -> hasRoute(GalleryGraph::class.qualifiedName.orEmpty(), null)
    MainTab.Settings -> hasRoute(SettingsGraph::class.qualifiedName.orEmpty(), null)
}

private fun NavDestination?.hasRoute(route: String, arguments: android.os.Bundle?): Boolean {
    return this?.hierarchy?.any { destination ->
        destination.hasRoute(route, arguments)
    } == true
}
