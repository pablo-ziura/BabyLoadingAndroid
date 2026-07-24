package com.example.babyloading.app.shell

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.babyloading.R
import com.example.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.example.babyloading.navigation.DashboardGraph
import com.example.babyloading.navigation.GalleryGraph
import com.example.babyloading.navigation.JourneyGraph
import com.example.babyloading.navigation.SettingsGraph

@Composable
internal fun MainScreen(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            MainNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        content = content
    )
}

@Composable
private fun MainNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.contentDescriptionRes)
                    )
                },
                label = { Text(text = stringResource(tab.labelRes)) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}

internal enum class MainTab(
    val graphRoute: Any,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    Dashboard(
        graphRoute = DashboardGraph,
        labelRes = R.string.dashboard_tab,
        contentDescriptionRes = R.string.dashboard_content_description,
        icon = Icons.Outlined.Dashboard,
        testTag = "dashboard_tab"
    ),
    Journey(
        graphRoute = JourneyGraph,
        labelRes = R.string.journey_tab,
        contentDescriptionRes = R.string.journey_content_description,
        icon = Icons.Outlined.Route,
        testTag = "journey_tab"
    ),
    Gallery(
        graphRoute = GalleryGraph,
        labelRes = R.string.gallery_tab,
        contentDescriptionRes = R.string.gallery_content_description,
        icon = Icons.Outlined.PhotoLibrary,
        testTag = "gallery_tab"
    ),
    Settings(
        graphRoute = SettingsGraph,
        labelRes = R.string.settings_tab,
        contentDescriptionRes = R.string.settings_content_description,
        icon = Icons.Outlined.Settings,
        testTag = "settings_tab"
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    BabyLoadingTheme {
        MainScreen(
            selectedTab = MainTab.Dashboard,
            onTabSelected = {},
            content = {}
        )
    }
}
