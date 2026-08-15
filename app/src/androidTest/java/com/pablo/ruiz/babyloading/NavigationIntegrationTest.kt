package com.pablo.ruiz.babyloading

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.navigation.AppNavigation
import com.pablo.ruiz.babyloading.navigation.MainShellGraph
import com.pablo.ruiz.babyloading.navigation.OnboardingRoute
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingEvent
import com.pablo.ruiz.babyloading.feature.onboarding.presentation.OnboardingUiState
import java.time.LocalDate
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
    private var receivedOnboardingEvent: OnboardingEvent? = null

    @Before
    fun setUp() {
        setNavigationContent()
    }

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
                    onboardingUiState = onboardingUiState,
                    onOnboardingEvent = { event -> receivedOnboardingEvent = event },
                    navController = testNavController,
                )
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
    fun continueRequestsDatePersistence() {
        composeTestRule.onNodeWithText(string(R.string.onboarding_continue)).performClick()

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

        composeTestRule.onNodeWithText(string(R.string.onboarding_continue)).assertIsNotEnabled()
    }

    @Test
    fun tabsShowTheirRootDestinationAndSelection() {
        setNavigationContent(startDestination = MainShellGraph)

        assertTabSelection("dashboard_tab", R.string.dashboard_title)
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
