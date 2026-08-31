package com.pablo.ruiz.babyloading.feature.tracking.data

import com.pablo.ruiz.babyloading.core.storage.AppStorageConfigFactory
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MediaStoreTrackingPhotoDataSourceTest {
    private val gateway = RecordingGateway()
    private val dataSource = MediaStoreTrackingPhotoDataSource(
        mediaStoreGateway = gateway,
        storageConfig = AppStorageConfigFactory().forApplicationId(
            AppStorageConfigFactory.ProductionApplicationId,
        ),
        ioDispatcher = Dispatchers.Unconfined,
    )

    @Test
    fun writesAndPublishesThePendingPhotoUsingExistingProductionNames() = runTest {
        val result = dataSource.exportJpegFromFile(
            privateFilePath = "/private/photo.jpg",
            capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
        )

        assertEquals("content://media/pending", result)
        assertEquals(listOf("create", "write", "publish"), gateway.operations)
        assertEquals(
            "BabyLoading_20260815_120000_000.jpg",
            gateway.photo?.displayName,
        )
        assertEquals("Pictures/Baby Loading", gateway.photo?.relativePath)
        assertEquals("/private/photo.jpg", gateway.privateFilePath)
    }

    @Test
    fun deletesThePendingUriWhenWritingFails() {
        gateway.writeFailure = IOException("Write failed")

        assertThrows(IOException::class.java) {
            runTest {
                dataSource.exportJpegFromFile(
                    privateFilePath = "/private/photo.jpg",
                    capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
                )
            }
        }
        assertEquals(listOf("create", "write", "delete"), gateway.operations)
    }

    private class RecordingGateway : TrackingMediaStoreGateway {
        val operations = mutableListOf<String>()
        var photo: TrackingMediaStorePhoto? = null
        var privateFilePath: String? = null
        var writeFailure: Throwable? = null

        override fun createPendingPhoto(photo: TrackingMediaStorePhoto): String {
            operations += "create"
            this.photo = photo
            return "content://media/pending"
        }

        override fun writeFromFile(uriValue: String, privateFilePath: String) {
            operations += "write"
            writeFailure?.let { throw it }
            this.privateFilePath = privateFilePath
        }

        override fun publish(uriValue: String) {
            operations += "publish"
        }

        override fun delete(uriValue: String) {
            operations += "delete"
        }
    }
}
