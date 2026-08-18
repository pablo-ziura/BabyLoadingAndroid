package com.pablo.ruiz.babyloading.feature.gallery.domain.model

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
