package com.pablo.ruiz.babyloading.feature.tracking.data

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface TrackingPhotoMediaStoreDataSource {
    suspend fun exportJpegFromFile(
        privateFilePath: String,
        capturedAt: Instant,
    ): String
}

@Singleton
class MediaStoreTrackingPhotoDataSource @Inject constructor(
    private val mediaStoreGateway: TrackingMediaStoreGateway,
    private val storageConfig: AppStorageConfig,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackingPhotoMediaStoreDataSource {
    override suspend fun exportJpegFromFile(
        privateFilePath: String,
        capturedAt: Instant,
    ): String = withContext(ioDispatcher) {
        require(privateFilePath.isNotBlank()) { "Exported photo path cannot be blank" }
        val uriValue = mediaStoreGateway.createPendingPhoto(
            TrackingMediaStorePhoto(
                displayName =
                    "${storageConfig.mediaStoreFilePrefix}_${FILE_NAME_FORMATTER.format(capturedAt)}.jpg",
                capturedAtEpochMillis = capturedAt.toEpochMilli(),
                relativePath =
                    "$PicturesDirectory/${storageConfig.mediaStoreDirectory}",
            ),
        )
        try {
            mediaStoreGateway.writeFromFile(uriValue, privateFilePath)
            mediaStoreGateway.publish(uriValue)
            uriValue
        } catch (error: Throwable) {
            runCatching { mediaStoreGateway.delete(uriValue) }
            throw error
        }
    }

    private companion object {
        const val PicturesDirectory = "Pictures"
        val FILE_NAME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss_SSS")
            .withZone(ZoneOffset.UTC)
    }
}

data class TrackingMediaStorePhoto(
    val displayName: String,
    val capturedAtEpochMillis: Long,
    val relativePath: String,
)

interface TrackingMediaStoreGateway {
    fun createPendingPhoto(photo: TrackingMediaStorePhoto): String

    fun writeFromFile(uriValue: String, privateFilePath: String)

    fun publish(uriValue: String)

    fun delete(uriValue: String)
}

@Singleton
class AndroidTrackingMediaStoreGateway @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TrackingMediaStoreGateway {
    override fun createPendingPhoto(photo: TrackingMediaStorePhoto): String {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photo.displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, photo.capturedAtEpochMillis)
            put(MediaStore.Images.Media.RELATIVE_PATH, photo.relativePath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        return context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?.toString()
            ?: throw IOException("Unable to create a MediaStore photo")
    }

    override fun writeFromFile(uriValue: String, privateFilePath: String) {
        val sourceFile = java.io.File(privateFilePath)
        require(sourceFile.isFile) { "Exported photo file does not exist" }
        sourceFile.inputStream().buffered().use { input ->
            context.contentResolver.openOutputStream(android.net.Uri.parse(uriValue), "w")
                ?.buffered()
                ?.use { output -> input.copyTo(output) }
                ?: throw IOException("Unable to open the MediaStore photo")
        }
    }

    override fun publish(uriValue: String) {
        val updatedRows = context.contentResolver.update(
            android.net.Uri.parse(uriValue),
            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
            null,
            null,
        )
        if (updatedRows <= 0) throw IOException("Unable to publish the MediaStore photo")
    }

    override fun delete(uriValue: String) {
        context.contentResolver.delete(android.net.Uri.parse(uriValue), null, null)
    }
}
