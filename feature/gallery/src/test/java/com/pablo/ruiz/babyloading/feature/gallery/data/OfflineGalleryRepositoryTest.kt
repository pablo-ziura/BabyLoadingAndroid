package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryFileDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryItemEntity
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryRoomDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.StoredGalleryImage
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfflineGalleryRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val operations = mutableListOf<String>()
    private val roomDataSource = FakeGalleryRoomDataSource(operations)
    private val fileDataSource by lazy {
        FakeGalleryFileDataSource(temporaryFolder.root, operations)
    }
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneOffset.UTC,
    )

    @Test
    fun importsDistinctPhotosAndReportsIndividualFailures() = runTest {
        val repository = createRepository()

        val result = repository.importPhotos(
            listOf("content://photos/one", "content://photos/one", "content://photos/bad"),
        )

        assertEquals(1, result.importedCount)
        assertEquals(1, result.failedCount)
        assertEquals(1, roomDataSource.entities.value.size)
        assertEquals(GallerySource.Imported.name, roomDataSource.entities.value.single().source)
    }

    @Test
    fun guidedPhotoStoresWeekAndDeletesOnlyPrivateFile() = runTest {
        val repository = createRepository()
        val capturedAt = Instant.parse("2026-08-15T12:00:00Z")

        val item = repository.addPrivatePhoto(
            data = byteArrayOf(1, 2, 3),
            source = GallerySource.GuidedTracking,
            capturedAt = capturedAt,
            pregnancyWeek = 24,
        )

        assertEquals(GallerySource.GuidedTracking, item.source)
        assertEquals(24, item.pregnancyWeek)
        assertEquals(listOf("file", "room"), operations)
        assertTrue(File(item.privateFilePath).exists())

        repository.deletePrivateCopy(item.id)

        assertTrue(roomDataSource.entities.value.isEmpty())
        assertFalse(File(item.privateFilePath).exists())
    }

    @Test
    fun unifiedFlowContainsImportedAndGuidedPhotos() = runTest {
        val repository = createRepository()

        repository.importPhotos(listOf("content://photos/imported"))
        repository.addPrivatePhoto(
            data = byteArrayOf(1, 2, 3),
            source = GallerySource.GuidedTracking,
            capturedAt = clock.instant(),
            pregnancyWeek = 24,
        )

        assertEquals(
            setOf(GallerySource.Imported, GallerySource.GuidedTracking),
            repository.items.first().map { it.source }.toSet(),
        )
    }

    @Test
    fun databaseFailureCleansUpNewPrivateFile() = runTest {
        roomDataSource.failInserts = true
        val repository = createRepository()

        val result = repository.importPhotos(listOf("content://photos/one"))

        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)
        assertTrue(fileDataSource.deletedFileNames.isNotEmpty())
    }

    @Test
    fun galleryItemsAreMappedOnTheInjectedIoDispatcher() = runTest {
        val dispatcher = RecordingDispatcher()
        val repository = createRepository(dispatcher)

        repository.items.first()

        assertTrue(dispatcher.dispatchCount > 0)
    }

    private fun createRepository(
        ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
    ): OfflineGalleryRepository {
        return OfflineGalleryRepository(
            roomDataSource = roomDataSource,
            fileDataSource = fileDataSource,
            mapper = GalleryItemMapper(fileDataSource),
            clock = clock,
            ioDispatcher = ioDispatcher,
        )
    }

    private class RecordingDispatcher : kotlinx.coroutines.CoroutineDispatcher() {
        var dispatchCount: Int = 0
            private set

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount += 1
            block.run()
        }
    }

    private class FakeGalleryRoomDataSource(
        private val operations: MutableList<String>,
    ) : GalleryRoomDataSource {
        val entities = MutableStateFlow<List<GalleryItemEntity>>(emptyList())
        var failInserts = false
        override val items: Flow<List<GalleryItemEntity>> = entities

        override suspend fun insert(item: GalleryItemEntity) {
            operations += "room"
            if (failInserts) throw IOException("Database unavailable")
            entities.value = listOf(item) + entities.value
        }

        override suspend fun itemById(id: String): GalleryItemEntity? {
            return entities.value.firstOrNull { it.id == id }
        }

        override suspend fun deleteById(id: String) {
            entities.value = entities.value.filterNot { it.id == id }
        }
    }

    private class FakeGalleryFileDataSource(
        private val directory: File,
        private val operations: MutableList<String>,
    ) : GalleryFileDataSource {
        val deletedFileNames = mutableListOf<String>()
        private var index = 0

        override suspend fun importFromUri(uriValue: String): StoredGalleryImage {
            if (uriValue.endsWith("bad")) throw IOException("Unreadable image")
            return createFile(byteArrayOf(1))
        }

        override suspend fun writeJpeg(data: ByteArray): StoredGalleryImage = createFile(data)

        override fun fileFor(fileName: String): File = File(directory, fileName)

        override suspend fun delete(fileName: String) {
            deletedFileNames += fileName
            fileFor(fileName).delete()
        }

        private fun createFile(data: ByteArray): StoredGalleryImage {
            operations += "file"
            val fileName = "image-${index++}.jpg"
            val file = fileFor(fileName).apply { writeBytes(data) }
            return StoredGalleryImage(fileName, file)
        }
    }
}
