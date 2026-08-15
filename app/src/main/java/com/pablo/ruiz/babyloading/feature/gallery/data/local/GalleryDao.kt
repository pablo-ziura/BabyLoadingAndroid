package com.pablo.ruiz.babyloading.feature.gallery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Query("SELECT * FROM gallery_items ORDER BY capturedAtEpochMillis DESC, id DESC")
    fun observeItems(): Flow<List<GalleryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: GalleryItemEntity)

    @Query("SELECT * FROM gallery_items WHERE id = :id LIMIT 1")
    suspend fun itemById(id: String): GalleryItemEntity?

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
