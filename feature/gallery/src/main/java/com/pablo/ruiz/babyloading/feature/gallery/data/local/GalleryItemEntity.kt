package com.pablo.ruiz.babyloading.feature.gallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gallery_items")
data class GalleryItemEntity(
    @PrimaryKey val id: String,
    val privateFileName: String,
    val capturedAtEpochMillis: Long,
    val source: String,
    val pregnancyWeek: Int?,
)
