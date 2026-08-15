package com.pablo.ruiz.babyloading.feature.journey.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage

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
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun JourneyContent(
    uiState: JourneyUiState,
    onEvent: (JourneyEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BabyLoadingBackground(modifier = modifier) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
            uiState.currentWeek == null -> Text(
                text = stringResource(R.string.journey_missing_setup),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(BabyLoadingSpacing.Large),
                style = MaterialTheme.typography.bodyLarge,
            )
            else -> JourneyTimeline(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun JourneyTimeline(
    uiState: JourneyUiState,
    onEvent: (JourneyEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.currentWeek, uiState.weeks) {
        if (uiState.weeks.isNotEmpty()) {
            val currentIndex = ((uiState.currentWeek ?: 1) - 2)
                .coerceIn(0, uiState.weeks.lastIndex)
            listState.scrollToItem(currentIndex)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(BabyLoadingSpacing.Large),
    ) {
        item {
            Text(
                text = stringResource(R.string.journey_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(
                    R.string.journey_current_position,
                    uiState.currentWeek ?: 0,
                    uiState.currentDay,
                ),
                modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                style = MaterialTheme.typography.bodyLarge,
            )
            JourneyStageNotice(stage = uiState.stage)
            Spacer(modifier = Modifier.height(BabyLoadingSpacing.Medium))
        }
        items(
            items = uiState.weeks,
            key = JourneyWeekUiModel::week,
        ) { week ->
            JourneyWeekRow(
                week = week,
                expanded = uiState.expandedWeek == week.week,
                onClick = { onEvent(JourneyEvent.WeekSelected(week.week)) },
            )
        }
    }
}

@Composable
private fun JourneyStageNotice(stage: PregnancyStage?) {
    val message = when (stage) {
        PregnancyStage.Early -> R.string.journey_stage_early
        PregnancyStage.PostTerm -> R.string.journey_stage_postterm
        PregnancyStage.NeedsReview -> R.string.journey_stage_review
        PregnancyStage.Active, null -> return
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = BabyLoadingSpacing.Medium),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = stringResource(message),
            modifier = Modifier.padding(BabyLoadingSpacing.Medium),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun JourneyWeekRow(
    week: JourneyWeekUiModel,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val statusDescription = stringResource(
        when (week.status) {
            JourneyWeekStatus.Completed -> R.string.journey_status_completed
            JourneyWeekStatus.Current -> R.string.journey_status_current
            JourneyWeekStatus.Upcoming -> R.string.journey_status_upcoming
        },
    )
    val accentColor = when (week.status) {
        JourneyWeekStatus.Completed -> MaterialTheme.colorScheme.secondary
        JourneyWeekStatus.Current -> MaterialTheme.colorScheme.primary
        JourneyWeekStatus.Upcoming -> MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        TimelineMarker(
            color = accentColor,
            current = week.status == JourneyWeekStatus.Current,
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
        )
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = BabyLoadingSpacing.Small)
                .animateContentSize()
                .semantics {
                    selected = week.status == JourneyWeekStatus.Current
                    stateDescription = statusDescription
                    role = Role.Button
                }
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (week.status == JourneyWeekStatus.Current) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (week.status == JourneyWeekStatus.Current) 8.dp else 2.dp,
            ),
        ) {
            Column(modifier = Modifier.padding(BabyLoadingSpacing.Medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    week.content?.let { content ->
                        Image(
                            painter = painterResource(content.babySize.drawableResource()),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.width(BabyLoadingSpacing.Medium))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.journey_week, week.week),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = week.content?.babySizeLabel ?: fallbackWeekSummary(week.week),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (week.status == JourneyWeekStatus.Current) {
                        Text(
                            text = stringResource(R.string.journey_you_are_here),
                            modifier = Modifier.padding(horizontal = BabyLoadingSpacing.Small),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = stringResource(
                            if (expanded) R.string.journey_collapse_week else R.string.journey_expand_week,
                        ),
                    )
                }
                if (expanded) {
                    ExpandedWeekContent(week = week)
                }
            }
        }
    }
}

@Composable
private fun TimelineMarker(
    color: Color,
    current: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 4.dp.toPx(),
        )
        drawCircle(
            color = color,
            radius = if (current) 8.dp.toPx() else 5.dp.toPx(),
            center = Offset(centerX, 32.dp.toPx()),
        )
        if (current) {
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = Offset(centerX, 32.dp.toPx()),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun ExpandedWeekContent(week: JourneyWeekUiModel) {
    Spacer(modifier = Modifier.height(BabyLoadingSpacing.Medium))
    week.content?.let { content ->
        Text(
            text = content.milestoneTitle,
            style = MaterialTheme.typography.titleMedium,
        )
        content.keyEvents.forEach { event ->
            Text(
                text = "• $event",
                modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        content.physiologicalImpact?.let { impact ->
            Text(
                text = impact,
                modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } ?: Text(
        text = if (week.week <= 5) {
            stringResource(R.string.journey_early_week_detail)
        } else {
            stringResource(R.string.journey_postterm_week_detail)
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun fallbackWeekSummary(week: Int): String {
    return stringResource(
        if (week <= 5) R.string.journey_early_week_summary else R.string.journey_postterm_week_summary,
    )
}

@Preview(showBackground = true)
@Composable
private fun JourneyScreenPreview() {
    BabyLoadingTheme {
        JourneyContent(
            uiState = JourneyUiState(
                isLoading = false,
                currentWeek = 20,
                currentDay = 3,
                stage = PregnancyStage.Active,
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
                expandedWeek = 20,
            ),
            onEvent = {},
        )
    }
}

private fun sampleContent(week: Int) = WeekContent(
    week = week,
    babySize = BabySize.Banana,
    babySizeLabel = "a banana",
    milestoneTitle = "A new milestone",
    keyEvents = listOf("Growth continues steadily."),
)
