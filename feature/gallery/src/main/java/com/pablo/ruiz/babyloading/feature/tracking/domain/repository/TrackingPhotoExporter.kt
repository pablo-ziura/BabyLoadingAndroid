package com.pablo.ruiz.babyloading.feature.tracking.domain.repository

import java.time.Instant

interface TrackingPhotoExporter {
    suspend fun exportJpeg(
        data: ByteArray,
        capturedAt: Instant,
    ): String
}
