package com.pablo.ruiz.babyloading.core.designsystem.component

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BabyLoadingDatePickerDialogTest {
    @Test
    fun datePickerUtcMillisRoundTripWithoutTimezoneConversion() {
        val date = LocalDate.of(2026, 5, 10)

        val restoredDate = date.toUtcDatePickerMillis().toLocalDateFromDatePicker()

        assertEquals(date, restoredDate)
    }
}
