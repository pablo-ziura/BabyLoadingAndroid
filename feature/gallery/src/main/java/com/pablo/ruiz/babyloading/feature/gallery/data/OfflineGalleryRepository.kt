package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryFileDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryRoomDataSource
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OfflineGalleryRepository @Inject constructor(
    private val roomDataSource: GalleryRoomDataSource,
    private val fileDataSource: GalleryFileDataSource,
    private val mapper: GalleryItemMapper,
    private val clock: Clock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GalleryRepository {
    override val items: Flow<List<GalleryItem>> = roomDataSource.items.map { entities ->
        entities.map(mapper::toDomain)
    }.flowOn(ioDispatcher)

    override suspend fun importPhotos(sourceUris: List<String>): GalleryImportResult {
        return withContext(ioDispatcher) {
            val distinctUris = sourceUris.distinct()
            var importedCount = 0
            distinctUris.forEach { uriValue ->
                try {
                    val storedImage = fileDataSource.importFromUri(uriValue)
                    val entity = mapper.toEntity(
                        id = UUID.randomUUID().toString(),
                        privateFileName = storedImage.fileName,
                        capturedAt = clock.instant(),
                        source = GallerySource.Imported,
                        pregnancyWeek = null,
                    )
                    try {
                        roomDataSource.insert(entity)
                    } catch (error: Throwable) {
                        fileDataSource.delete(storedImage.fileName)
                        throw error
                    }
                    importedCount += 1
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // Continue importing the remaining selections and report this item as failed.
                }
            }
            GalleryImportResult(
                importedCount = importedCount,
                failedCount = distinctUris.size - importedCount,
            )
        }
    }

    override suspend fun addPrivatePhotoFromFile(
        temporaryFilePath: String,
        source: GallerySource,
        capturedAt: Instant,
        pregnancyWeek: Int?,
    ): GalleryItem = withContext(ioDispatcher) {
        val storedImage = fileDataSource.writeJpegFromFile(temporaryFilePath)
        val entity = mapper.toEntity(
            id = UUID.randomUUID().toString(),
            privateFileName = storedImage.fileName,
            capturedAt = capturedAt,
            source = source,
            pregnancyWeek = pregnancyWeek,
        )
        try {
            roomDataSource.insert(entity)
        } catch (error: Throwable) {
            fileDataSource.delete(storedImage.fileName)
            throw error
        }
        mapper.toDomain(entity)
    }

    override suspend fun deletePrivateCopy(id: String) = withContext(ioDispatcher) {
        val entity = roomDataSource.itemById(id) ?: return@withContext
        fileDataSource.delete(entity.privateFileName)
        roomDataSource.deleteById(id)
    }
}
