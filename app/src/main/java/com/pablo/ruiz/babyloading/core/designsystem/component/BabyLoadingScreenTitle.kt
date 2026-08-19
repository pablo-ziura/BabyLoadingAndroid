package com.pablo.ruiz.babyloading.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme

@Composable
fun BabyLoadingScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isProminent: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
            style = if (isProminent) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.titleLarge
            },
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BabyLoadingSpacing.ExtraSmall),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BabyLoadingScreenTitlePreview() {
    BabyLoadingTheme {
        BabyLoadingScreenTitle(
            title = stringResource(R.string.dashboard_title),
            subtitle = stringResource(R.string.dashboard_subtitle),
        )
    }
}
