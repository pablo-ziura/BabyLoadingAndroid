package com.pablo.ruiz.babyloading.feature.gallery.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class TrackingCadence(val intervalDays: Int) {
    Weekly(7),
    EveryTwoWeeks(14),
    EveryFourWeeks(28),
    ;

    companion object {
        val Default = Weekly

        fun fromIntervalDays(intervalDays: Int): TrackingCadence {
            return entries.firstOrNull { it.intervalDays == intervalDays } ?: Default
        }
    }
}

sealed interface TrackingStatus {
    data object NeedsInitialCapture : TrackingStatus

    data class UpToDate(val nextDueDate: LocalDate) : TrackingStatus

    data class Pending(val nextDueDate: LocalDate) : TrackingStatus
}

fun TrackingCadence.trackingStatus(
    lastCapture: Instant?,
    asOf: Instant,
    zoneId: ZoneId,
): TrackingStatus {
    lastCapture ?: return TrackingStatus.NeedsInitialCapture

    val captureDay = lastCapture.atZone(zoneId).toLocalDate()
    val currentDay = asOf.atZone(zoneId).toLocalDate()
    val elapsedDays = maxOf(0, ChronoUnit.DAYS.between(captureDay, currentDay))
    val nextDueDate = captureDay.plusDays(intervalDays.toLong())

    return if (elapsedDays <= intervalDays) {
        TrackingStatus.UpToDate(nextDueDate)
    } else {
        TrackingStatus.Pending(nextDueDate)
    }
}
