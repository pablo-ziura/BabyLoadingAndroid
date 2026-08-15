package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryDao
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryImageStore
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryItemEntity
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OfflineGalleryRepository @Inject constructor(
    private val dao: GalleryDao,
    private val imageStore: GalleryImageStore,
    private val clock: Clock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GalleryRepository {
    override val items: Flow<List<GalleryItem>> = dao.observeItems().map { entities ->
        entities.map { entity ->
            entity.toDomain(
                privateFilePath = imageStore.fileFor(entity.privateFileName).absolutePath,
            )
        }
    }

    override suspend fun importPhotos(sourceUris: List<String>): GalleryImportResult {
        return withContext(ioDispatcher) {
            var importedCount = 0
            sourceUris.distinct().forEach { uriValue ->
                val imported = runCatching {
                    val storedImage = imageStore.importFromUri(uriValue)
                    val entity = GalleryItemEntity(
                        id = UUID.randomUUID().toString(),
                        privateFileName = storedImage.fileName,
                        capturedAtEpochMillis = clock.millis(),
                        source = GallerySource.Imported.name,
                        pregnancyWeek = null,
                    )
                    try {
                        dao.insert(entity)
                    } catch (error: Throwable) {
                        imageStore.delete(storedImage.fileName)
                        throw error
                    }
                }.isSuccess
                if (imported) importedCount += 1
            }
            GalleryImportResult(
                importedCount = importedCount,
                failedCount = sourceUris.distinct().size - importedCount,
            )
        }
    }

    override suspend fun addPrivatePhoto(
        data: ByteArray,
        source: GallerySource,
        capturedAt: Instant,
        pregnancyWeek: Int?,
    ): GalleryItem = withContext(ioDispatcher) {
        val storedImage = imageStore.writeJpeg(data)
        val entity = GalleryItemEntity(
            id = UUID.randomUUID().toString(),
            privateFileName = storedImage.fileName,
            capturedAtEpochMillis = capturedAt.toEpochMilli(),
            source = source.name,
            pregnancyWeek = pregnancyWeek,
        )
        try {
            dao.insert(entity)
        } catch (error: Throwable) {
            imageStore.delete(storedImage.fileName)
            throw error
        }
        entity.toDomain(storedImage.file.absolutePath)
    }

    override suspend fun deleteItem(id: String) = withContext(ioDispatcher) {
        val entity = dao.itemById(id) ?: return@withContext
        dao.deleteById(id)
        imageStore.delete(entity.privateFileName)
    }
}
