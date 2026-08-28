package com.pablo.ruiz.babyloading.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyGradientBottom
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyGradientTop
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme

@Composable
fun BabyLoadingBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BabyGradientTop, BabyGradientBottom),
                ),
            ),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun BabyLoadingBackgroundPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground(content = {})
    }
}
