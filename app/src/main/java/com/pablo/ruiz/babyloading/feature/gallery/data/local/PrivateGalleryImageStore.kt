package com.pablo.ruiz.babyloading.feature.gallery.data.local

import android.content.Context
import androidx.core.net.toUri
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class PrivateGalleryImageStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GalleryImageStore {
    private val galleryDirectory: File
        get() = File(context.filesDir, GALLERY_DIRECTORY).apply { mkdirs() }

    override suspend fun importFromUri(uriValue: String): StoredGalleryImage = withContext(ioDispatcher) {
        val uri = uriValue.toUri()
        val extension = when (context.contentResolver.getType(uri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic", "image/heif" -> "heic"
            else -> "jpg"
        }
        val storedImage = createTarget(extension)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                storedImage.file.outputStream().buffered().use { output ->
                    copyWithLimit(input, output)
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
        require(data.size <= MAX_IMAGE_BYTES) { "Image exceeds the private storage limit" }
        createTarget("jpg").also { storedImage ->
            storedImage.file.writeBytes(data)
        }
    }

    override fun fileFor(fileName: String): File {
        require(fileName == File(fileName).name) { "Gallery file name must not contain a path" }
        return File(galleryDirectory, fileName)
    }

    override suspend fun delete(fileName: String) = withContext(ioDispatcher) {
        fileFor(fileName).delete()
        Unit
    }

    private fun createTarget(extension: String): StoredGalleryImage {
        val fileName = "${UUID.randomUUID()}.$extension"
        val file = fileFor(fileName)
        return StoredGalleryImage(fileName = fileName, file = file)
    }

    private fun copyWithLimit(
        input: java.io.InputStream,
        output: java.io.OutputStream,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0L
        while (true) {
            val bytesRead = input.read(buffer)
            if (bytesRead < 0) break
            totalBytes += bytesRead
            if (totalBytes > MAX_IMAGE_BYTES) {
                throw IOException("Selected image exceeds the private storage limit")
            }
            output.write(buffer, 0, bytesRead)
        }
    }

    private companion object {
        const val GALLERY_DIRECTORY = "gallery"
        const val MAX_IMAGE_BYTES = 25 * 1024 * 1024
    }
}
