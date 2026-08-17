package com.pablo.ruiz.babyloading.app.shell

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.navigation.DashboardGraph
import com.pablo.ruiz.babyloading.navigation.GalleryGraph
import com.pablo.ruiz.babyloading.navigation.JourneyGraph
import com.pablo.ruiz.babyloading.navigation.SettingsGraph

@Composable
internal fun MainScreen(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        BabyLoadingBackground(content = {})
        when (MainNavigationType.forWidth(maxWidth)) {
            MainNavigationType.BottomBar -> CompactMainScreen(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                content = content,
            )

            MainNavigationType.Rail -> ExpandedMainScreen(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                content = content,
            )
        }
    }
}

@Composable
private fun CompactMainScreen(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            MainNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
            )
        },
        content = content,
    )
}

@Composable
private fun ExpandedMainScreen(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        MainNavigationRail(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
        )
        Scaffold(
            modifier = Modifier.weight(1f),
            containerColor = Color.Transparent,
            content = content,
        )
    }
}

@Composable
private fun MainNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.contentDescriptionRes),
                    )
                },
                label = { Text(text = stringResource(tab.labelRes)) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(tab.testTag),
            )
        }
    }
}

@Composable
private fun MainNavigationRail(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        Spacer(modifier = Modifier.height(24.dp))
        Column {
            MainTab.entries.forEach { tab ->
                NavigationRailItem(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(tab.contentDescriptionRes),
                        )
                    },
                    label = { Text(text = stringResource(tab.labelRes)) },
                    alwaysShowLabel = true,
                    modifier = Modifier.testTag(tab.testTag),
                )
            }
        }
    }
}

internal enum class MainTab(
    val graphRoute: Any,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
    val testTag: String,
) {
    Dashboard(
        graphRoute = DashboardGraph,
        labelRes = R.string.dashboard_tab,
        contentDescriptionRes = R.string.dashboard_content_description,
        icon = Icons.Filled.Favorite,
        testTag = "dashboard_tab",
    ),
    Journey(
        graphRoute = JourneyGraph,
        labelRes = R.string.journey_tab,
        contentDescriptionRes = R.string.journey_content_description,
        icon = Icons.Filled.Map,
        testTag = "journey_tab",
    ),
    Gallery(
        graphRoute = GalleryGraph,
        labelRes = R.string.gallery_tab,
        contentDescriptionRes = R.string.gallery_content_description,
        icon = Icons.Filled.PhotoLibrary,
        testTag = "gallery_tab",
    ),
    Settings(
        graphRoute = SettingsGraph,
        labelRes = R.string.settings_tab,
        contentDescriptionRes = R.string.settings_content_description,
        icon = Icons.Filled.Settings,
        testTag = "settings_tab",
    ),
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    BabyLoadingTheme {
        MainScreen(
            selectedTab = MainTab.Dashboard,
            onTabSelected = {},
            content = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
private fun ExpandedMainScreenPreview() {
    BabyLoadingTheme {
        MainScreen(
            selectedTab = MainTab.Journey,
            onTabSelected = {},
            content = {},
        )
    }
}
