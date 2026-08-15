package com.pablo.ruiz.babyloading.app.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class MainNavigationType {
    BottomBar,
    Rail;

    companion object {
        private val RailBreakpoint = 600.dp

        fun forWidth(width: Dp): MainNavigationType {
            return if (width < RailBreakpoint) BottomBar else Rail
        }
    }
}
