package com.pablo.ruiz.babyloading.feature.onboarding.presentation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object OnboardingDateFormatter {
    fun format(date: LocalDate, locale: Locale): String {
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale),
        )
    }
}
