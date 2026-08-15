package com.pablo.ruiz.babyloading.app.shell

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class MainNavigationTypeTest {
    @Test
    fun compactWidthUsesBottomBar() {
        assertEquals(MainNavigationType.BottomBar, MainNavigationType.forWidth(599.dp))
    }

    @Test
    fun mediumAndExpandedWidthsUseRail() {
        assertEquals(MainNavigationType.Rail, MainNavigationType.forWidth(600.dp))
        assertEquals(MainNavigationType.Rail, MainNavigationType.forWidth(840.dp))
    }
}
