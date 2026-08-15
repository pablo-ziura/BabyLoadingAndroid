package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DashboardDateFormatter {
    fun format(date: LocalDate, locale: Locale): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale),
        )
    }
}
