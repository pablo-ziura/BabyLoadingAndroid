package com.pablo.ruiz.babyloading.feature.settings.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.BuildConfig
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.localization.AppLanguage
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingScreenTitle
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyAccentPink
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyAccentPurple
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onOpenLanguageSettings = {
            context.startActivity(
                Intent(
                    Settings.ACTION_APP_LOCALE_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onOpenLanguageSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.settings_date_saved)

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            snackbarHostState.showSnackbar(savedMessage)
            onEvent(SettingsEvent.SaveMessageShown)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                SettingsList(
                    uiState = uiState,
                    onSaveDate = { onEvent(SettingsEvent.SaveDate) },
                    onDateSelected = { date -> onEvent(SettingsEvent.DateSelected(date)) },
                    onOpenLanguageSettings = onOpenLanguageSettings,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

@Composable
private fun SettingsList(
    uiState: SettingsUiState,
    onSaveDate: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onOpenLanguageSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ENGLISH
    val openLanguageSettingsDescription = stringResource(
        R.string.settings_language_open_settings_description,
    )
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(BabyLoadingSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Large),
    ) {
        item {
            BabyLoadingScreenTitle(title = stringResource(R.string.settings_title))
        }
        item {
            BabyLoadingCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
            ) {
                Text(
                    text = stringResource(R.string.settings_last_period_prompt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (uiState.hasStoredFutureDate) {
                    Text(
                        text = stringResource(R.string.settings_invalid_future_last_period_date),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = BabyLoadingSpacing.Small),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Small))
                SettingsCalendar(
                    selectedDate = uiState.selectedDate,
                    minimumDate = uiState.minimumDate,
                    maximumDate = uiState.maximumDate,
                    estimatedDueDate = uiState.estimatedDueDate,
                    locale = locale,
                    onDateSelected = onDateSelected,
                )
                uiState.validationError?.let { error ->
                    Text(
                        text = stringResource(
                            when (error) {
                                SettingsValidationError.FutureDate -> R.string.onboarding_future_date_error
                                SettingsValidationError.DateTooOld -> R.string.onboarding_old_date_error
                            },
                        ),
                        modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            SetDateButton(
                isSaving = uiState.isSaving,
                enabled = uiState.canSave,
                onClick = onSaveDate,
            )
        }
        item {
            BabyLoadingCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
            ) {
                LanguageSectionHeader()
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Small))
                Text(
                    text = stringResource(
                        R.string.settings_language_current,
                        appLanguageName(uiState.appLanguage),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.settings_language_system_message),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onOpenLanguageSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BabyLoadingSpacing.Medium)
                        .semantics {
                            contentDescription = openLanguageSettingsDescription
                        },
                ) {
                    Text(
                        text = stringResource(R.string.settings_language_open_settings),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        item {
            SettingsInformation()
        }
        item {
            Spacer(modifier = Modifier.height(BabyLoadingSpacing.ExtraLarge))
        }
    }
}

@Composable
private fun SettingsCalendar(
    selectedDate: LocalDate?,
    minimumDate: LocalDate,
    maximumDate: LocalDate,
    estimatedDueDate: LocalDate?,
    locale: Locale,
    onDateSelected: (LocalDate) -> Unit,
) {
    val selectedMonth = selectedDate?.let(YearMonth::from)
    val minimumMonth = listOfNotNull(YearMonth.from(minimumDate), selectedMonth).minOrNull()
        ?: YearMonth.from(minimumDate)
    val maximumMonth = YearMonth.from(maximumDate)
    var displayedMonth by remember(selectedDate) {
        mutableStateOf(selectedMonth ?: maximumMonth)
    }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val weekdayFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE", locale) }
    val calendarDates = remember(displayedMonth) { displayedMonth.calendarDates() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = displayedMonth.format(monthFormatter),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(
            onClick = { displayedMonth = displayedMonth.minusMonths(1) },
            enabled = displayedMonth > minimumMonth,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = stringResource(R.string.settings_previous_month),
                tint = BabyAccentPink,
            )
        }
        IconButton(
            onClick = { displayedMonth = displayedMonth.plusMonths(1) },
            enabled = displayedMonth < maximumMonth,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(R.string.settings_next_month),
                tint = BabyAccentPink,
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(DaysInWeek) { index ->
            Text(
                text = weekdayFormatter.format(WeekdayReferenceDate.plusDays(index.toLong()))
                    .uppercase(locale),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    Column {
        calendarDates.chunked(DaysInWeek).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CalendarDayHeight),
            ) {
                week.forEach { date ->
                    CalendarDate(
                        date = date,
                        selectedDate = selectedDate,
                        minimumDate = minimumDate,
                        maximumDate = maximumDate,
                        locale = locale,
                        onDateSelected = onDateSelected,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
    estimatedDueDate?.let { dueDate ->
        HorizontalDivider(
            modifier = Modifier.padding(vertical = BabyLoadingSpacing.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.settings_due_date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = SettingsDateFormatter.format(dueDate, locale),
                modifier = Modifier.padding(top = BabyLoadingSpacing.ExtraSmall),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun CalendarDate(
    date: LocalDate?,
    selectedDate: LocalDate?,
    minimumDate: LocalDate,
    maximumDate: LocalDate,
    locale: Locale,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (date == null) {
        Spacer(modifier = modifier)
        return
    }

    val isSelected = date == selectedDate
    val isSelectable = date in minimumDate..maximumDate
    val selectedStateDescription = stringResource(R.string.settings_selected_date)
    val textColor = when {
        isSelected -> BabyAccentPink
        isSelectable -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) BabyAccentPink.copy(alpha = 0.12f) else Color.Transparent,
                shape = CircleShape,
            )
            .semantics {
                contentDescription = SettingsDateFormatter.format(date, locale)
                selected = isSelected
                if (isSelected) {
                    stateDescription = selectedStateDescription
                }
            }
            .clickable(
                enabled = isSelectable,
                role = Role.Button,
                onClick = { onDateSelected(date) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
        )
    }
}

@Composable
private fun SetDateButton(
    isSaving: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val gradient = if (enabled) {
        listOf(BabyAccentPink, BabyAccentPurple.copy(alpha = 0.8f))
    } else {
        listOf(
            BabyAccentPink.copy(alpha = DisabledActionAlpha),
            BabyAccentPurple.copy(alpha = DisabledActionAlpha * 0.8f),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = BabyAccentPink.copy(alpha = 0.4f),
                spotColor = BabyAccentPink.copy(alpha = 0.4f),
            )
            .clip(CircleShape)
            .background(Brush.horizontalGradient(gradient))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = BabyLoadingSpacing.Medium),
        contentAlignment = Alignment.Center,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(R.string.settings_save_date),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun LanguageSectionHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(BabyAccentPink.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = BabyAccentPink,
            )
        }
        Text(
            text = stringResource(R.string.settings_language_title),
            modifier = Modifier
                .padding(start = BabyLoadingSpacing.Small)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun appLanguageName(language: AppLanguage): String = stringResource(
    when (language) {
        AppLanguage.English -> R.string.settings_language_english
        AppLanguage.Spanish -> R.string.settings_language_spanish
    },
)

@Composable
private fun SettingsInformation() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = stringResource(R.string.settings_info),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = BabyLoadingSpacing.Small,
                    start = BabyLoadingSpacing.ExtraLarge,
                    end = BabyLoadingSpacing.ExtraLarge,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun YearMonth.calendarDates(): List<LocalDate?> {
    val leadingEmptyDays = atDay(1).dayOfWeek.value - 1
    val trailingEmptyDays = (DaysInWeek - (leadingEmptyDays + lengthOfMonth()) % DaysInWeek) % DaysInWeek
    return buildList {
        repeat(leadingEmptyDays) { add(null) }
        for (day in 1..lengthOfMonth()) {
            add(atDay(day))
        }
        repeat(trailingEmptyDays) { add(null) }
    }
}

private const val DaysInWeek = 7
private const val DisabledActionAlpha = 0.38f
private val CalendarDayHeight = 48.dp
private val WeekdayReferenceDate = LocalDate.of(2024, 1, 1)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            SettingsContent(
                uiState = SettingsUiState(
                    isLoading = false,
                    savedDate = LocalDate.of(2026, 5, 10),
                    selectedDate = LocalDate.of(2026, 5, 12),
                    minimumDate = LocalDate.of(2025, 10, 25),
                    maximumDate = LocalDate.of(2026, 8, 15),
                    estimatedDueDate = LocalDate.of(2027, 2, 16),
                ),
                onEvent = {},
                onOpenLanguageSettings = {},
            )
        }
    }
}
