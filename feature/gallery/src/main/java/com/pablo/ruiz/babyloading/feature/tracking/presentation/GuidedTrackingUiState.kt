package com.pablo.ruiz.babyloading.feature.tracking.presentation

import com.pablo.ruiz.babyloading.feature.tracking.domain.model.CapturedPhotoFile

data class GuidedTrackingUiState(
    val pregnancyWeek: Int? = null,
    val referenceImagePath: String? = null,
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
    data class PhotoCaptured(val photo: CapturedPhotoFile) : GuidedTrackingEvent

    data object CaptureFailed : GuidedTrackingEvent

    data object ErrorShown : GuidedTrackingEvent
}
