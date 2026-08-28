package com.pablo.ruiz.babyloading

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.navigation.AppNavigation
import com.pablo.ruiz.babyloading.navigation.MainNavigation
import com.pablo.ruiz.babyloading.navigation.OnboardingRoute
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingEvent
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingScreen
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingUiState
import com.pablo.ruiz.babyloading.feature.onboarding.R as OnboardingR
import com.pablo.ruiz.babyloading.feature.dashboard.R as DashboardR
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavHostController
    private var receivedOnboardingEvent: OnboardingEvent? = null

    private fun setNavigationContent(
        startDestination: Any = OnboardingRoute,
        onboardingUiState: OnboardingUiState = OnboardingUiState(
            isLoading = false,
            selectedDate = LocalDate.of(2026, 5, 10),
            maximumDate = LocalDate.of(2026, 8, 15),
        ),
    ) {
        composeTestRule.setContent {
            val testNavController = TestNavHostController(composeTestRule.activity).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            navController = testNavController

            BabyLoadingTheme {
                AppNavigation(
                    startDestination = startDestination,
                    onboardingContent = {
                        OnboardingScreen(
                            uiState = onboardingUiState,
                            onEvent = { event -> receivedOnboardingEvent = event },
                        )
                    },
                    navController = testNavController,
                )
            }
        }
    }

    @Test
    fun onboardingIsTheInitialDestination() {
        setNavigationContent()

        composeTestRule.onNodeWithText(string(OnboardingR.string.onboarding_title)).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(navController.currentDestination.matches(OnboardingRoute::class.qualifiedName.orEmpty()))
        }
    }

    @Test
    fun continueRequestsDatePersistence() {
        setNavigationContent()

        composeTestRule.onNodeWithText(string(OnboardingR.string.onboarding_continue)).performClick()

        composeTestRule.runOnIdle {
            assertTrue(receivedOnboardingEvent == OnboardingEvent.Continue)
        }
    }

    @Test
    fun continueRequiresASelectedDate() {
        setNavigationContent(
            onboardingUiState = OnboardingUiState(
                isLoading = false,
                maximumDate = LocalDate.of(2026, 8, 15),
            ),
        )

        composeTestRule.onNodeWithText(string(OnboardingR.string.onboarding_continue)).assertIsNotEnabled()
    }

    @Test
    fun tabsShowTheirRootDestinationAndSelection() {
        setMainTabsContent()

        assertTabSelection("dashboard_tab", "dashboard_screen")
        assertTabSelection("journey_tab", "journey_screen")
        assertTabSelection("gallery_tab", "gallery_screen")
        assertTabSelection("settings_tab", "settings_screen")
    }

    private fun setMainTabsContent() {
        composeTestRule.setContent {
            val testNavController = TestNavHostController(composeTestRule.activity).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            navController = testNavController

            BabyLoadingTheme {
                MainNavigation(
                    navController = testNavController,
                    dashboardContent = {
                        Text(
                            text = stringResource(DashboardR.string.dashboard_title),
                            modifier = Modifier.testTag("dashboard_screen"),
                        )
                    },
                    journeyContent = {
                        Text(
                            text = stringResource(R.string.journey_title),
                            modifier = Modifier.testTag("journey_screen"),
                        )
                    },
                    galleryContent = {
                        Text(
                            text = stringResource(R.string.gallery_title),
                            modifier = Modifier.testTag("gallery_screen"),
                        )
                    },
                    guidedTrackingContent = {},
                    settingsContent = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            modifier = Modifier.testTag("settings_screen"),
                        )
                    },
                )
            }
        }
    }

    private fun assertTabSelection(tabTag: String, screenTag: String) {
        composeTestRule.onNodeWithTag(tabTag).performClick()
        composeTestRule.onNodeWithTag(tabTag).assertIsSelected()
        composeTestRule.onNodeWithTag(screenTag).assertIsDisplayed()
    }

    private fun string(resourceId: Int): String {
        return composeTestRule.activity.getString(resourceId)
    }

    private fun NavDestination?.matches(route: String): Boolean {
        return this?.hasRoute(route, null) == true
    }
}
