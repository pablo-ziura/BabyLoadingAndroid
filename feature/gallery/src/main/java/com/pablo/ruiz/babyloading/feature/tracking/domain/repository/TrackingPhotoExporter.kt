package com.pablo.ruiz.babyloading.feature.tracking.domain.repository

import java.time.Instant

interface TrackingPhotoExporter {
    suspend fun exportJpegFromFile(
        privateFilePath: String,
        capturedAt: Instant,
    ): String
}
