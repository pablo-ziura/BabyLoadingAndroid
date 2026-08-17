package com.pablo.ruiz.babyloading.feature.settings.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.BuildConfig
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingDatePickerDialog
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingScreenTitle
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import java.time.LocalDate

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
    var showDatePicker by remember { mutableStateOf(false) }
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                SettingsList(
                    uiState = uiState,
                    onChooseDate = { showDatePicker = true },
                    onSaveDate = { onEvent(SettingsEvent.SaveDate) },
                    onOpenLanguageSettings = onOpenLanguageSettings,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    if (showDatePicker) {
        BabyLoadingDatePickerDialog(
            selectedDate = uiState.selectedDate,
            minimumDate = uiState.minimumDate,
            maximumDate = uiState.maximumDate,
            onDateSelected = { date ->
                onEvent(SettingsEvent.DateSelected(date))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun SettingsList(
    uiState: SettingsUiState,
    onChooseDate: () -> Unit,
    onSaveDate: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
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
            BabyLoadingScreenTitle(title = stringResource(R.string.settings_title))
        }
        item {
            BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(
                    icon = Icons.Outlined.CalendarMonth,
                    title = stringResource(R.string.settings_pregnancy_title),
                )
                Text(
                    text = stringResource(R.string.settings_last_period_prompt),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(
                    onClick = onChooseDate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BabyLoadingSpacing.Small),
                ) {
                    Text(
                        text = uiState.selectedDate?.let { date ->
                            SettingsDateFormatter.format(date, locale)
                        } ?: stringResource(R.string.settings_choose_date),
                    )
                }
                uiState.estimatedDueDate?.let { dueDate ->
                    Text(
                        text = stringResource(R.string.settings_due_date),
                        modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = SettingsDateFormatter.format(dueDate, locale),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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
                Button(
                    onClick = onSaveDate,
                    enabled = uiState.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BabyLoadingSpacing.Large),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(R.string.settings_save_date))
                    }
                }
            }
        }
        item {
            BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language_title),
                )
                Text(
                    text = stringResource(R.string.settings_language_message),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = onOpenLanguageSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BabyLoadingSpacing.Medium),
                ) {
                    Text(text = stringResource(R.string.settings_language_action))
                }
            }
        }
        item {
            BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.settings_privacy_title),
                )
                Text(
                    text = stringResource(R.string.settings_privacy_message),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.ExtraSmall),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(BabyLoadingSpacing.Large)) }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

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
