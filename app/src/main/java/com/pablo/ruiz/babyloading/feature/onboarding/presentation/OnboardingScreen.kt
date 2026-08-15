package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.PregnantWoman
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import java.time.LocalDate
import java.util.Locale

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()

    BabyLoadingBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(BabyLoadingSpacing.Large),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BabyLoadingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PregnantWoman,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Medium))
                Text(
                    text = stringResource(R.string.onboarding_title),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Small))
                Text(
                    text = stringResource(R.string.onboarding_message),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Large))
                Text(
                    text = stringResource(R.string.onboarding_date_label),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Small))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                    )
                    Text(
                        text = uiState.selectedDate?.let { date ->
                            OnboardingDateFormatter.format(date, locale)
                        } ?: stringResource(R.string.onboarding_choose_date),
                        modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
                    )
                }
                if (uiState.validationError != null) {
                    Text(
                        text = stringResource(
                            when (uiState.validationError) {
                                OnboardingValidationError.FutureDate -> R.string.onboarding_future_date_error
                                OnboardingValidationError.DateTooOld -> R.string.onboarding_old_date_error
                            },
                        ),
                        modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(BabyLoadingSpacing.Large))
                Button(
                    onClick = { onEvent(OnboardingEvent.Continue) },
                    enabled = uiState.canContinue,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(R.string.onboarding_continue))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        LastPeriodDatePickerDialog(
            selectedDate = uiState.selectedDate,
            minimumDate = uiState.minimumDate,
            maximumDate = uiState.maximumDate,
            onDateSelected = { date ->
                onEvent(OnboardingEvent.DateSelected(date))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun LastPeriodDatePickerDialog(
    selectedDate: LocalDate?,
    minimumDate: LocalDate,
    maximumDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toUtcDatePickerMillis(),
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in minimumDate.toUtcDatePickerMillis()..maximumDate.toUtcDatePickerMillis()
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDateFromDatePicker()
                        ?.let(onDateSelected)
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

internal fun LocalDate.toUtcDatePickerMillis(): Long = toEpochDay() * MILLIS_PER_DAY

internal fun Long.toLocalDateFromDatePicker(): LocalDate = LocalDate.ofEpochDay(this / MILLIS_PER_DAY)

private const val MILLIS_PER_DAY = 86_400_000L

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    BabyLoadingTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(
                isLoading = false,
                selectedDate = LocalDate.of(2026, 5, 10),
                minimumDate = LocalDate.of(2025, 10, 25),
                maximumDate = LocalDate.of(2026, 8, 15),
            ),
            onEvent = {},
        )
    }
}
