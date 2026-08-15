package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage
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
    BabyLoadingBackground(modifier = modifier) {
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
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        item {
            StageNotice(stage = progress.stage)
        }
        item {
            ProgressHero(
                progress = progress,
                weekContent = weekContent,
            )
        }
        item {
            ProgressFacts(
                progress = progress,
                locale = locale,
            )
        }
        if (weekContent != null) {
            item { MilestoneCard(content = weekContent) }
            item { KeyEventsCard(content = weekContent) }
            weekContent.physiologicalImpact?.let { impact ->
                item { PhysiologicalImpactCard(impact = impact) }
            }
        } else {
            item {
                BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.dashboard_early_content),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun StageNotice(stage: PregnancyStage) {
    val message = when (stage) {
        PregnancyStage.Early -> R.string.dashboard_stage_early
        PregnancyStage.Active -> return
        PregnancyStage.PostTerm -> R.string.dashboard_stage_postterm
        PregnancyStage.NeedsReview -> R.string.dashboard_stage_review
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = stringResource(message),
            modifier = Modifier.padding(BabyLoadingSpacing.Medium),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ProgressHero(
    progress: PregnancyProgress,
    weekContent: WeekContent?,
) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                R.string.dashboard_week_and_day,
                progress.gestationalAge.completedWeeks,
                progress.gestationalAge.daysIntoWeek,
            ),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
        )
        weekContent?.let { content ->
            Image(
                painter = painterResource(content.babySize.drawableResource()),
                contentDescription = content.babySizeLabel,
                modifier = Modifier
                    .size(176.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.dashboard_baby_size, content.babySizeLabel),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(BabyLoadingSpacing.Medium))
        LinearProgressIndicator(
            progress = { progress.completedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .progressSemantics(progress.completedFraction),
        )
        Text(
            text = stringResource(
                R.string.dashboard_progress_percent,
                (progress.completedFraction * 100).toInt(),
            ),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ProgressFacts(
    progress: PregnancyProgress,
    locale: Locale,
) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        DashboardFact(
            icon = Icons.Outlined.Schedule,
            label = stringResource(R.string.dashboard_days_remaining),
            value = progress.daysRemaining.toString(),
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = BabyLoadingSpacing.Medium))
        DashboardFact(
            icon = Icons.Outlined.Event,
            label = stringResource(R.string.dashboard_due_date),
            value = DashboardDateFormatter.format(progress.estimatedDueDate, locale),
        )
    }
}

@Composable
private fun DashboardFact(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(BabyLoadingSpacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MilestoneCard(content: WeekContent) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_milestone),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = content.milestoneTitle,
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun KeyEventsCard(content: WeekContent) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_this_week),
            style = MaterialTheme.typography.titleLarge,
        )
        content.keyEvents.forEach { event ->
            Row(
                modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = event,
                    modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun PhysiologicalImpactCard(impact: String) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_why_it_matters),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = impact,
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    BabyLoadingTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                progress = PregnancyProgress(
                    lastPeriodDate = LocalDate.of(2026, 3, 1),
                    estimatedDueDate = LocalDate.of(2026, 12, 6),
                    gestationalAge = GestationalAge(24, 3, 171),
                    daysRemaining = 109,
                    completedFraction = 0.61f,
                    stage = PregnancyStage.Active,
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
