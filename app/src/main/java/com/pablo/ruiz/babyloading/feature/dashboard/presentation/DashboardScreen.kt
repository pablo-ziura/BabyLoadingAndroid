package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingScreenTitle
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.ActivePregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import java.time.LocalDate
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(DashboardEvent.Refresh)
    }
    DashboardContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            uiState.progress == null -> Text(
                text = stringResource(R.string.dashboard_missing_setup),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(BabyLoadingSpacing.Large),
                style = MaterialTheme.typography.bodyLarge,
            )

            else -> DashboardProgress(
                progress = uiState.progress,
                weekContent = uiState.weekContent,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun DashboardProgress(
    progress: PregnancyProgress,
    weekContent: WeekContent?,
    modifier: Modifier = Modifier,
) {
    val locale = checkNotNull(LocalConfiguration.current.locales[0])
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(BabyLoadingSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
    ) {
        item {
            BabyLoadingScreenTitle(
                title = stringResource(R.string.dashboard_title),
                subtitle = stringResource(R.string.dashboard_subtitle),
                isProminent = true,
            )
        }
        when (progress) {
            is PregnancyProgress.Active -> {
                val activeProgress = progress.progress
                if (activeProgress.phase != PregnancyPhase.Ongoing) {
                    item {
                        PregnancyProgressStatusCard(
                            phase = activeProgress.phase,
                        )
                    }
                }
                if (weekContent != null) {
                    item {
                        ProgressHero(weekContent = weekContent)
                    }
                }
                item {
                    ProgressFacts(
                        progress = activeProgress,
                        locale = locale,
                    )
                }
                if (weekContent != null) {
                    item { DevelopmentCard(content = weekContent) }
                }
            }

            is PregnancyProgress.InvalidFutureLastPeriodDate -> item {
                InvalidDateCard()
            }
        }
    }
}

@Composable
private fun PregnancyProgressStatusCard(phase: PregnancyPhase) {
    val title = when (phase) {
        PregnancyPhase.Ongoing -> return
        PregnancyPhase.LateTerm -> R.string.pregnancy_status_late_term_title
        PregnancyPhase.PostTerm -> R.string.pregnancy_status_post_term_title
    }
    val message = when (phase) {
        PregnancyPhase.Ongoing -> return
        PregnancyPhase.LateTerm -> R.string.pregnancy_status_late_term_message
        PregnancyPhase.PostTerm -> R.string.pregnancy_status_post_term_message
    }

    BabyLoadingCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        containerColor = Color.White.copy(alpha = 0.88f),
    ) {
        Text(
            text = stringResource(title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(message),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InvalidDateCard() {
    BabyLoadingCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        containerColor = Color.White.copy(alpha = 0.88f),
    ) {
        Text(
            text = stringResource(R.string.pregnancy_status_invalid_date_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.pregnancy_status_invalid_date_message),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressHero(
    weekContent: WeekContent,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = BabyLoadingSpacing.Medium,
                vertical = BabyLoadingSpacing.Large,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
        ) {
            BabySizeArtwork(babySize = weekContent.babySize)
            Text(
                text = stringResource(R.string.dashboard_baby_size, weekContent.babySizeLabel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BabyLoadingSpacing.Medium),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BabySizeArtwork(babySize: BabySize) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        0.6f to Color.White.copy(alpha = 0.4f),
                        1f to Color.Transparent,
                    ),
                ),
        )
        Surface(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = BabyLoadingSpacing.Medium,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                ),
            shape = CircleShape,
            color = Color.White,
            content = {},
        )
        Image(
            painter = painterResource(babySize.drawableResource()),
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun ProgressFacts(
    progress: ActivePregnancyProgress,
    locale: Locale,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
        ) {
            DashboardStatCard(
                icon = Icons.Outlined.Event,
                label = stringResource(R.string.dashboard_week),
                value = progress.gestationalAge.completedWeeks.toString(),
                modifier = Modifier.weight(1f),
            )
            DueDateRelationStatCard(
                relation = progress.dueDateRelation,
                modifier = Modifier.weight(1f),
            )
        }
        DueDateCard(
            dueDate = DashboardDateFormatter.format(progress.estimatedDueDate, locale),
        )
    }
}

@Composable
private fun DueDateRelationStatCard(
    relation: DueDateRelation,
    modifier: Modifier = Modifier,
) {
    val label: String
    val value: String
    when (relation) {
        is DueDateRelation.Upcoming -> {
            label = stringResource(R.string.dashboard_days_until_due_date)
            value = relation.days.toString()
        }

        DueDateRelation.Today -> {
            label = stringResource(R.string.dashboard_due_date_metric)
            value = stringResource(R.string.pregnancy_status_due_date_today)
        }

        is DueDateRelation.Elapsed -> {
            label = stringResource(R.string.dashboard_days_since_due_date)
            value = relation.days.toString()
        }
    }
    DashboardStatCard(
        icon = Icons.Outlined.Schedule,
        label = label,
        value = value,
        modifier = modifier,
    )
}

@Composable
private fun DashboardStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = DashboardMetricCardMinHeight)
            .shadow(
                elevation = 12.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BabyLoadingSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Small),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White,
                )
            }
            Text(
                text = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = DashboardMetricBandHeight)
                    .wrapContentHeight(Alignment.CenterVertically),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = DashboardMetricBandHeight)
                    .wrapContentHeight(Alignment.Top),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DueDateCard(dueDate: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BabyLoadingSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.ExtraSmall),
        ) {
            Text(
                text = stringResource(R.string.dashboard_due_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = dueDate,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DevelopmentCard(content: WeekContent) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(BabyLoadingSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🐣",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = content.milestoneTitle,
                    modifier = Modifier
                        .padding(start = BabyLoadingSpacing.Small)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Small),
            ) {
                content.keyEvents.forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(
                                horizontal = BabyLoadingSpacing.Medium,
                                vertical = BabyLoadingSpacing.Small,
                            )
                            .semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "✨",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = event,
                            modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            content.physiologicalImpact?.let { impact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f))
                        .padding(BabyLoadingSpacing.Medium)
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "💕",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = impact,
                        modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            DashboardContent(
                uiState = DashboardUiState(
                    isLoading = false,
                    progress = PregnancyProgress.Active(
                        ActivePregnancyProgress(
                            lastPeriodDate = LocalDate.of(2026, 3, 1),
                            estimatedDueDate = LocalDate.of(2026, 12, 6),
                            gestationalAge = GestationalAge(24, 3, 171),
                            phase = PregnancyPhase.Ongoing,
                            dueDateRelation = DueDateRelation.Upcoming(109),
                        ),
                    ),
                    weekContent = WeekContent(
                        week = 24,
                        babySize = BabySize.Corn,
                        babySizeLabel = "an ear of corn",
                        milestoneTitle = "Lung development continues",
                        keyEvents = listOf(
                            "The lungs continue forming their smallest branches.",
                            "Hearing becomes more responsive.",
                        ),
                        physiologicalImpact = "These changes prepare the baby for life after birth.",
                    ),
                ),
            )
        }
    }
}

private val DashboardMetricBandHeight = 48.dp
private val DashboardMetricCardMinHeight = 112.dp
