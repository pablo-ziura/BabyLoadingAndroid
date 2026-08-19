package com.pablo.ruiz.babyloading.feature.gallery.presentation

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object GalleryDateFormatter {
    fun format(
        date: LocalDate,
        locale: Locale,
    ): String {
        return DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(date)
    }

    fun format(
        instant: Instant,
        locale: Locale,
        zoneId: ZoneId,
    ): String {
        return DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(zoneId)
            .format(instant)
    }
}
