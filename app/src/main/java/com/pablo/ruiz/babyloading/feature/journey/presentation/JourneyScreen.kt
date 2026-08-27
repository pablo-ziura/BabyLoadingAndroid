package com.pablo.ruiz.babyloading.feature.journey.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyAccentPink
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyAccentPurple
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import java.util.Locale

@Composable
fun JourneyScreen(
    modifier: Modifier = Modifier,
    viewModel: JourneyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(JourneyEvent.Refresh)
    }
    JourneyContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
private fun JourneyContent(
    uiState: JourneyUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
            !uiState.isConfigured -> Text(
                text = stringResource(R.string.journey_missing_setup),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(BabyLoadingSpacing.Large),
                style = MaterialTheme.typography.bodyLarge,
            )
            else -> JourneyTimeline(
                uiState = uiState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun JourneyTimeline(
    uiState: JourneyUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(
            horizontal = BabyLoadingSpacing.Medium,
            vertical = BabyLoadingSpacing.Large,
        ),
    ) {
        item {
            Text(
                text = stringResource(R.string.journey_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = BabyLoadingSpacing.Medium)
                    .semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
        items(
            items = uiState.weeks,
            key = JourneyWeekUiModel::week,
        ) { week ->
            JourneyWeekRow(
                week = week,
                currentWeek = uiState.currentWeek,
                currentDay = uiState.currentDay,
            )
        }
    }
}

@Composable
private fun JourneyWeekRow(
    week: JourneyWeekUiModel,
    currentWeek: Int?,
    currentDay: Int,
) {
    val isCurrent = week.status == JourneyWeekStatus.Current
    val locale = checkNotNull(LocalConfiguration.current.locales[0])

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
    ) {
        JourneyTimelineMarker(
            week = week.week,
            currentWeek = currentWeek,
            currentDay = currentDay,
            isCurrent = isCurrent,
            modifier = Modifier
                .width(BabyLoadingSpacing.Medium)
                .fillMaxHeight(),
        )
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = BabyLoadingSpacing.ExtraSmall)
                .shadow(
                    elevation = if (isCurrent) BabyLoadingSpacing.Small else 0.dp,
                    shape = MaterialTheme.shapes.medium,
                    ambientColor = BabyAccentPink.copy(alpha = 0.15f),
                    spotColor = BabyAccentPink.copy(alpha = 0.15f),
                )
                .semantics(mergeDescendants = true) {
                    selected = isCurrent
                },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isCurrent) 1f else 0.88f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = BabyLoadingSpacing.Medium,
                    vertical = BabyLoadingSpacing.Small,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JourneyBabySizeImage(babySize = week.content.babySize)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = BabyLoadingSpacing.Medium),
                ) {
                    Text(
                        text = stringResource(R.string.journey_week, week.week),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatJourneyBabySizeLabel(week.content.babySizeLabel, locale),
                        modifier = Modifier.padding(top = BabyLoadingSpacing.ExtraSmall),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isCurrent) {
                    Text(
                        text = stringResource(R.string.journey_you_are_here),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        BabyAccentPink,
                                        BabyAccentPurple.copy(alpha = 0.8f),
                                    ),
                                ),
                            )
                            .padding(
                                horizontal = BabyLoadingSpacing.Small,
                                vertical = BabyLoadingSpacing.ExtraSmall,
                            )
                            .padding(start = BabyLoadingSpacing.Small),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyBabySizeImage(babySize: BabySize) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(babySize.drawableResource()),
            contentDescription = null,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.5.dp,
                    color = BabyAccentPink.copy(alpha = 0.2f),
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun JourneyTimelineMarker(
    week: Int,
    currentWeek: Int?,
    currentDay: Int,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 2.dp.toPx(),
        )

        (0..2).forEach { index ->
            drawTimelineDay(
                centerX = centerX,
                centerY = centerY * (index + 1) / 4,
                highlighted = currentWeek != null && week == currentWeek + 1 && currentDay - 4 == index,
                accent = BabyAccentPink,
            )
        }
        if (isCurrent) {
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(centerX, centerY),
            )
            drawCircle(
                color = BabyAccentPink,
                radius = 5.dp.toPx(),
                center = Offset(centerX, centerY),
            )
        } else {
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = 4.dp.toPx(),
                center = Offset(centerX, centerY),
            )
        }
        (0..3).forEach { index ->
            drawTimelineDay(
                centerX = centerX,
                centerY = centerY + (centerY * (index + 1) / 4),
                highlighted = currentWeek != null && week == currentWeek && currentDay == index,
                accent = BabyAccentPink,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTimelineDay(
    centerX: Float,
    centerY: Float,
    highlighted: Boolean,
    accent: Color,
) {
    drawCircle(
        color = if (highlighted) accent else Color.White.copy(alpha = 0.65f),
        radius = if (highlighted) 3.5.dp.toPx() else 2.dp.toPx(),
        center = Offset(centerX, centerY),
    )
    if (highlighted) {
        drawCircle(
            color = Color.White,
            radius = 4.5.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

internal fun formatJourneyBabySizeLabel(label: String, locale: Locale): String {
    return Regex("\\p{L}+").replace(label) { word ->
        word.value.replaceFirstChar { it.titlecase(locale) }
    }
}

@Preview(showBackground = true)
@Composable
private fun JourneyScreenPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            JourneyContent(
                uiState = JourneyUiState(
                    isLoading = false,
                    isConfigured = true,
                    currentWeek = 20,
                    currentDay = 3,
                    weeks = listOf(
                        JourneyWeekUiModel(
                            week = 19,
                            status = JourneyWeekStatus.Completed,
                            content = sampleContent(19),
                        ),
                        JourneyWeekUiModel(
                            week = 20,
                            status = JourneyWeekStatus.Current,
                            content = sampleContent(20),
                        ),
                        JourneyWeekUiModel(
                            week = 21,
                            status = JourneyWeekStatus.Upcoming,
                            content = sampleContent(21),
                        ),
                    ),
                ),
            )
        }
    }
}

private fun sampleContent(week: Int) = WeekContent(
    week = week,
    babySize = BabySize.Banana,
    babySizeLabel = "a banana",
    milestoneTitle = "A new milestone",
    keyEvents = listOf("Growth continues steadily."),
)
