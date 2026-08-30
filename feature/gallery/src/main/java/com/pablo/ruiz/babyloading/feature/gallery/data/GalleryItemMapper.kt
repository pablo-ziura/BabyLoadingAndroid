package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryFileDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryItemEntity
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant
import javax.inject.Inject

class GalleryItemMapper @Inject constructor(
    private val fileDataSource: GalleryFileDataSource,
) {
    fun toDomain(entity: GalleryItemEntity): GalleryItem {
        return GalleryItem(
            id = entity.id,
            privateFilePath = fileDataSource.fileFor(entity.privateFileName).absolutePath,
            capturedAt = Instant.ofEpochMilli(entity.capturedAtEpochMillis),
            source = GallerySource.valueOf(entity.source),
            pregnancyWeek = entity.pregnancyWeek,
        )
    }

    fun toEntity(
        id: String,
        privateFileName: String,
        capturedAt: Instant,
        source: GallerySource,
        pregnancyWeek: Int?,
    ): GalleryItemEntity {
        return GalleryItemEntity(
            id = id,
            privateFileName = privateFileName,
            capturedAtEpochMillis = capturedAt.toEpochMilli(),
            source = source.name,
            pregnancyWeek = pregnancyWeek,
        )
    }
}
