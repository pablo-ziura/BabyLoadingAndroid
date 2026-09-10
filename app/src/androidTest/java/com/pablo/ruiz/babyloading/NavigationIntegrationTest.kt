package com.pablo.ruiz.babyloading

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.navigation.AppNavigation
import com.pablo.ruiz.babyloading.navigation.GuidedTrackingRoute
import com.pablo.ruiz.babyloading.navigation.MainNavigation
import com.pablo.ruiz.babyloading.navigation.OnboardingRoute
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingEvent
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingScreen
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingUiState
import com.pablo.ruiz.babyloading.feature.onboarding.R as OnboardingR
import com.pablo.ruiz.babyloading.feature.dashboard.R as DashboardR
import com.pablo.ruiz.babyloading.feature.journey.R as JourneyR
import com.pablo.ruiz.babyloading.feature.gallery.R as GalleryR
import com.pablo.ruiz.babyloading.feature.settings.R as SettingsR
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
    private var isJourneySelected = false
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
            val testNavController = remember {
                TestNavHostController(composeTestRule.activity).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
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
            assertTrue(navController.currentDestination?.hasRoute<OnboardingRoute>() == true)
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
        assertTabSelection("journey_tab", "journey_screen")
        assertTabSelection("dashboard_tab", "dashboard_screen")
    }

    @Test
    fun guidedTrackingHidesNavigationAndReturnsToSelectedGallery() {
        setMainTabsContent()
        assertTabSelection("gallery_tab", "gallery_screen")

        composeTestRule.runOnIdle { navController.navigate(GuidedTrackingRoute) }
        composeTestRule.onNodeWithTag("guided_tracking_screen").assertIsDisplayed()
        listOf("dashboard_tab", "journey_tab", "gallery_tab", "settings_tab")
            .forEach { composeTestRule.onNodeWithTag(it).assertDoesNotExist() }

        composeTestRule.runOnIdle { navController.navigateUp() }
        composeTestRule.onNodeWithTag("gallery_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gallery_tab").assertIsSelected()
    }

    private fun setMainTabsContent() {
        composeTestRule.setContent {
            val testNavController = remember {
                TestNavHostController(composeTestRule.activity).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
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
                    journeyContent = { selected ->
                        isJourneySelected = selected
                        Text(
                            text = stringResource(JourneyR.string.journey_title),
                            modifier = Modifier.testTag("journey_screen"),
                        )
                    },
                    galleryContent = {
                        Text(
                            text = stringResource(GalleryR.string.gallery_title),
                            modifier = Modifier.testTag("gallery_screen"),
                        )
                    },
                    guidedTrackingContent = {
                        Text(
                            text = "Guided tracking",
                            modifier = Modifier.testTag("guided_tracking_screen"),
                        )
                    },
                    settingsContent = {
                        Text(
                            text = stringResource(SettingsR.string.settings_title),
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
        listOf("dashboard_tab", "journey_tab", "gallery_tab", "settings_tab")
            .filterNot { it == tabTag }
            .forEach { composeTestRule.onNodeWithTag(it).assertIsNotSelected() }
        if (tabTag == "journey_tab") {
            composeTestRule.runOnIdle { assertTrue(isJourneySelected) }
        }
    }

    private fun string(resourceId: Int): String {
        return composeTestRule.activity.getString(resourceId)
    }
}
