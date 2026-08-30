package com.pablo.ruiz.babyloading.feature.gallery.data.local

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface GalleryRoomDataSource {
    val items: Flow<List<GalleryItemEntity>>

    suspend fun insert(item: GalleryItemEntity)

    suspend fun itemById(id: String): GalleryItemEntity?

    suspend fun deleteById(id: String)
}

@Singleton
class RoomGalleryDataSource @Inject constructor(
    private val dao: GalleryDao,
) : GalleryRoomDataSource {
    override val items: Flow<List<GalleryItemEntity>> = dao.observeItems()

    override suspend fun insert(item: GalleryItemEntity) = dao.insert(item)

    override suspend fun itemById(id: String): GalleryItemEntity? = dao.itemById(id)

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}
