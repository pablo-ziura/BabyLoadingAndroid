package com.pablo.ruiz.babyloading.feature.tracking.presentation

data class TrackingGuideGeometry(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float = right - left
    val height: Float = bottom - top
}

fun calculateTrackingGuideGeometry(
    width: Float,
    height: Float,
): TrackingGuideGeometry {
    require(width > 0f && height > 0f) { "Guide dimensions must be positive" }
    val availableAspectRatio = width / height
    val guideWidth: Float
    val guideHeight: Float
    if (availableAspectRatio > GUIDED_PHOTO_ASPECT_RATIO) {
        guideHeight = height
        guideWidth = height * GUIDED_PHOTO_ASPECT_RATIO
    } else {
        guideWidth = width
        guideHeight = width / GUIDED_PHOTO_ASPECT_RATIO
    }
    val left = (width - guideWidth) / 2f
    val top = (height - guideHeight) / 2f
    return TrackingGuideGeometry(
        left = left,
        top = top,
        right = left + guideWidth,
        bottom = top + guideHeight,
    )
}

const val GUIDED_PHOTO_ASPECT_RATIO = 9f / 16f
