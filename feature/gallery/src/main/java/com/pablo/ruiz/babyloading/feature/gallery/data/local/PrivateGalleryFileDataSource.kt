package com.pablo.ruiz.babyloading.feature.gallery.data.local

import android.content.Context
import androidx.core.net.toUri
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class PrivateGalleryFileDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val storageConfig: AppStorageConfig,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GalleryFileDataSource {
    private val pathResolver = GalleryFilePathResolver(
        rootDirectory = context.filesDir,
        directoryName = storageConfig.privateGalleryDirectory,
    )

    override suspend fun importFromUri(uriValue: String): StoredGalleryImage = withContext(ioDispatcher) {
        val uri = uriValue.toUri()
        val extension = galleryFileExtension(context.contentResolver.getType(uri))
        val storedImage = createTarget(extension)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                storedImage.file.outputStream().buffered().use { output ->
                    copyGalleryImageWithLimit(input, output)
                }
            } ?: throw IOException("Unable to open selected image")
            storedImage
        } catch (error: Throwable) {
            storedImage.file.delete()
            throw error
        }
    }

    override suspend fun writeJpeg(data: ByteArray): StoredGalleryImage = withContext(ioDispatcher) {
        require(data.isNotEmpty()) { "Image data cannot be empty" }
        require(data.size <= MaximumPrivateGalleryImageBytes) {
            "Image exceeds the private storage limit"
        }
        createTarget("jpg").also { storedImage ->
            storedImage.file.writeBytes(data)
        }
    }

    override fun fileFor(fileName: String): File {
        return pathResolver.fileFor(fileName)
    }

    override suspend fun delete(fileName: String) = withContext(ioDispatcher) {
        fileFor(fileName).delete()
        Unit
    }

    private fun createTarget(extension: String): StoredGalleryImage {
        val fileName = "${UUID.randomUUID()}.$extension"
        val file = File(pathResolver.ensureDirectory(), fileName)
        return StoredGalleryImage(fileName = fileName, file = file)
    }

}

internal const val MaximumPrivateGalleryImageBytes = 25 * 1024 * 1024

internal fun galleryFileExtension(mimeType: String?): String = when (mimeType) {
    "image/jpeg", "image/jpg", null -> "jpg"
    "image/png" -> "png"
    "image/webp" -> "webp"
    "image/heic", "image/heif" -> "heic"
    else -> if (mimeType.startsWith("image/")) {
        "jpg"
    } else {
        throw IOException("Selected content is not an image")
    }
}

internal fun copyGalleryImageWithLimit(
    input: java.io.InputStream,
    output: java.io.OutputStream,
    maximumBytes: Int = MaximumPrivateGalleryImageBytes,
) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var totalBytes = 0L
    while (true) {
        val bytesRead = input.read(buffer)
        if (bytesRead < 0) break
        totalBytes += bytesRead
        if (totalBytes > maximumBytes) {
            throw IOException("Selected image exceeds the private storage limit")
        }
        output.write(buffer, 0, bytesRead)
    }
}
