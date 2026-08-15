package com.pablo.ruiz.babyloading.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mainDispatcherUsesControllableTestScheduler() = runTest {
        var completed = false

        launch(Dispatchers.Main) {
            completed = true
        }

        assertFalse(completed)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        assertTrue(completed)
    }
}
