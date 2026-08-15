package com.pablo.ruiz.babyloading.feature.gallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant

@Entity(tableName = "gallery_items")
data class GalleryItemEntity(
    @PrimaryKey val id: String,
    val privateFileName: String,
    val capturedAtEpochMillis: Long,
    val source: String,
    val pregnancyWeek: Int?,
) {
    fun toDomain(privateFilePath: String): GalleryItem {
        return GalleryItem(
            id = id,
            privateFilePath = privateFilePath,
            capturedAt = Instant.ofEpochMilli(capturedAtEpochMillis),
            source = GallerySource.valueOf(source),
            pregnancyWeek = pregnancyWeek,
        )
    }
}
