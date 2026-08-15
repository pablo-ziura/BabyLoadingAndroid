package com.pablo.ruiz.babyloading.feature.gallery.data

import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryDao
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryImageStore
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryItemEntity
import com.pablo.ruiz.babyloading.feature.gallery.data.local.StoredGalleryImage
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfflineGalleryRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dao = FakeGalleryDao()
    private val imageStore by lazy { FakeGalleryImageStore(temporaryFolder.root) }
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
        assertEquals(1, dao.entities.value.size)
        assertEquals(GallerySource.Imported.name, dao.entities.value.single().source)
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
        assertTrue(File(item.privateFilePath).exists())

        repository.deleteItem(item.id)

        assertTrue(dao.entities.value.isEmpty())
        assertFalse(File(item.privateFilePath).exists())
    }

    @Test
    fun databaseFailureCleansUpNewPrivateFile() = runTest {
        dao.failInserts = true
        val repository = createRepository()

        val result = repository.importPhotos(listOf("content://photos/one"))

        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)
        assertTrue(imageStore.deletedFileNames.isNotEmpty())
    }

    private fun createRepository(): OfflineGalleryRepository {
        return OfflineGalleryRepository(
            dao = dao,
            imageStore = imageStore,
            clock = clock,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
    }

    private class FakeGalleryDao : GalleryDao {
        val entities = MutableStateFlow<List<GalleryItemEntity>>(emptyList())
        var failInserts = false

        override fun observeItems(): Flow<List<GalleryItemEntity>> = entities

        override suspend fun insert(item: GalleryItemEntity) {
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

    private class FakeGalleryImageStore(
        private val directory: File,
    ) : GalleryImageStore {
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
            val fileName = "image-${index++}.jpg"
            val file = fileFor(fileName).apply { writeBytes(data) }
            return StoredGalleryImage(fileName, file)
        }
    }
}
