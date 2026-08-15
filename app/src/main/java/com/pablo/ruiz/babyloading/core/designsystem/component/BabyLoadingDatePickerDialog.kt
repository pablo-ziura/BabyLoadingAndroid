package com.pablo.ruiz.babyloading.core.designsystem.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.time.LocalDate

@Composable
fun BabyLoadingDatePickerDialog(
    selectedDate: LocalDate?,
    minimumDate: LocalDate,
    maximumDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toUtcDatePickerMillis(),
        selectableDates = object : SelectableDates {
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
