package com.pablo.ruiz.babyloading.feature.tracking.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.storage.AppStorageNames
import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class MediaStoreTrackingPhotoExporter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackingPhotoExporter {
    override suspend fun exportJpeg(
        data: ByteArray,
        capturedAt: Instant,
    ): String = withContext(ioDispatcher) {
        require(data.isNotEmpty()) { "Exported photo cannot be empty" }
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${AppStorageNames.current.mediaStoreFilePrefix}_${FILE_NAME_FORMATTER.format(capturedAt)}.jpg",
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, capturedAt.toEpochMilli())
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/${AppStorageNames.current.mediaStoreDirectory}",
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create a MediaStore photo")
        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                output.write(data)
            } ?: throw IOException("Unable to open the MediaStore photo")
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
            uri.toString()
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private companion object {
        val FILE_NAME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss_SSS")
            .withZone(ZoneOffset.UTC)
    }
}
