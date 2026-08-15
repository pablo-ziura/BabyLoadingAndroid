package com.pablo.ruiz.babyloading.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme

@Composable
fun BabyLoadingCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = BabyLoadingSpacing.Small),
    ) {
        Column(
            modifier = Modifier.padding(BabyLoadingSpacing.Medium),
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BabyLoadingCardPreview() {
    BabyLoadingTheme {
        BabyLoadingCard {
            Text(
                text = "Week 24",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Your baby is growing every day.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
