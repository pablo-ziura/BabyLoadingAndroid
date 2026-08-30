package com.pablo.ruiz.babyloading.feature.tracking.data

import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTrackingPhotoExporter @Inject constructor(
    private val dataSource: TrackingPhotoMediaStoreDataSource,
) : TrackingPhotoExporter {
    override suspend fun exportJpeg(data: ByteArray, capturedAt: Instant): String {
        return dataSource.exportJpeg(data, capturedAt)
    }
}
