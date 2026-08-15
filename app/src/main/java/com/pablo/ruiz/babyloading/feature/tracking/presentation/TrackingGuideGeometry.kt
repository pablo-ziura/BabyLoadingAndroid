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
    val guideWidth = width * 0.68f
    val guideHeight = height * 0.66f
    val left = (width - guideWidth) / 2f
    val top = height * 0.16f
    return TrackingGuideGeometry(
        left = left,
        top = top,
        right = left + guideWidth,
        bottom = top + guideHeight,
    )
}
