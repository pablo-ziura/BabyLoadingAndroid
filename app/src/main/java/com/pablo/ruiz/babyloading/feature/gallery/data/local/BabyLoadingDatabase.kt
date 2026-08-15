package com.pablo.ruiz.babyloading.feature.gallery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GalleryItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BabyLoadingDatabase : RoomDatabase() {
    abstract fun galleryDao(): GalleryDao
}
