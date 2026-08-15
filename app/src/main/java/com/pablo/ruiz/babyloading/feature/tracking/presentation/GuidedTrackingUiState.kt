package com.pablo.ruiz.babyloading.feature.tracking.presentation

data class GuidedTrackingUiState(
    val pregnancyWeek: Int? = null,
    val isSaving: Boolean = false,
    val saveOutcome: GuidedTrackingSaveOutcome? = null,
    val error: GuidedTrackingError? = null,
)

enum class GuidedTrackingSaveOutcome {
    PrivateAndPublic,
    PrivateOnly,
}

enum class GuidedTrackingError {
    CaptureFailed,
    SaveFailed,
}

sealed interface GuidedTrackingEvent {
    data class PhotoCaptured(val data: ByteArray) : GuidedTrackingEvent

    data object CaptureFailed : GuidedTrackingEvent

    data object ErrorShown : GuidedTrackingEvent
}
