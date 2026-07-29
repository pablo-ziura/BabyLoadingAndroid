package com.example.babyloading

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.babyloading.navigation.AppNavigation
import com.example.babyloading.navigation.MainShellGraph
import com.example.babyloading.navigation.OnboardingRoute
import com.example.babyloading.core.designsystem.theme.BabyLoadingTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavHostController

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val testNavController = TestNavHostController(composeTestRule.activity).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            navController = testNavController

            BabyLoadingTheme {
                AppNavigation(navController = testNavController)
            }
        }
    }

    @Test
    fun onboardingIsTheInitialDestination() {
        composeTestRule.onNodeWithText(string(R.string.onboarding_title)).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(navController.currentDestination.matches(OnboardingRoute::class.qualifiedName.orEmpty()))
        }
    }

    @Test
    fun continueRemovesOnboardingFromBackStack() {
        composeTestRule.onNodeWithText(string(R.string.onboarding_continue)).performClick()

        composeTestRule.onNodeWithText(string(R.string.dashboard_placeholder)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(string(R.string.onboarding_title)).assertCountEquals(0)
        composeTestRule.runOnIdle {
            assertTrue(navController.currentDestination.matches(MainShellGraph::class.qualifiedName.orEmpty()))
            assertFalse(navController.previousBackStackEntry?.destination.matches(OnboardingRoute::class.qualifiedName.orEmpty()))
        }
    }

    @Test
    fun tabsShowTheirRootDestinationAndSelection() {
        composeTestRule.onNodeWithText(string(R.string.onboarding_continue)).performClick()

        assertTabSelection("dashboard_tab", R.string.dashboard_placeholder)
        assertTabSelection("journey_tab", R.string.journey_placeholder)
        assertTabSelection("gallery_tab", R.string.gallery_placeholder)
        assertTabSelection("settings_tab", R.string.settings_placeholder)
    }

    private fun assertTabSelection(tabTag: String, screenTitleRes: Int) {
        composeTestRule.onNodeWithTag(tabTag).performClick()
        composeTestRule.onNodeWithTag(tabTag).assertIsSelected()
        composeTestRule.onNodeWithText(string(screenTitleRes)).assertIsDisplayed()
    }

    private fun string(resourceId: Int): String {
        return composeTestRule.activity.getString(resourceId)
    }

    private fun NavDestination?.matches(route: String): Boolean {
        return this?.hasRoute(route, null) == true
    }
}
