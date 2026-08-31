package com.pablo.ruiz.babyloading.feature.gallery.data.local

import android.content.Context
import androidx.core.net.toUri
import com.pablo.ruiz.babyloading.core.coroutines.IoDispatcher
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
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

    override suspend fun writeJpegFromFile(
        temporaryFilePath: String,
    ): StoredGalleryImage = withContext(ioDispatcher) {
        val sourceFile = File(temporaryFilePath)
        val storedImage = createTarget("jpg")
        storeTemporaryCapture(
            sourceFile = sourceFile,
            targetFile = storedImage.file,
            cacheDirectory = context.cacheDir,
        )
        storedImage
    }

    override fun fileFor(fileName: String): File {
        return pathResolver.fileFor(fileName)
    }

    override suspend fun delete(fileName: String) = withContext(ioDispatcher) {
        Files.deleteIfExists(fileFor(fileName).toPath())
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
): Long {
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
    return totalBytes
}

internal fun requireTemporaryCaptureFile(sourceFile: File, cacheDirectory: File) {
    require(sourceFile.name.startsWith("guided-capture-") && sourceFile.extension == "jpg") {
        "Captured photo must use the guided capture file name"
    }
    require(sourceFile.canonicalFile.parentFile == cacheDirectory.canonicalFile) {
        "Captured photo must be stored in the application cache directory"
    }
    require(sourceFile.isFile) { "Captured photo file does not exist" }
}

internal fun storeTemporaryCapture(
    sourceFile: File,
    targetFile: File,
    cacheDirectory: File,
    maximumBytes: Int = MaximumPrivateGalleryImageBytes,
) {
    requireTemporaryCaptureFile(sourceFile, cacheDirectory)
    try {
        sourceFile.inputStream().buffered().use { input ->
            targetFile.outputStream().buffered().use { output ->
                val copiedBytes = copyGalleryImageWithLimit(input, output, maximumBytes)
                require(copiedBytes > 0) { "Captured photo cannot be empty" }
            }
        }
        Files.delete(sourceFile.toPath())
    } catch (error: Throwable) {
        runCatching { Files.deleteIfExists(targetFile.toPath()) }
            .exceptionOrNull()
            ?.let(error::addSuppressed)
        runCatching { Files.deleteIfExists(sourceFile.toPath()) }
            .exceptionOrNull()
            ?.let(error::addSuppressed)
        throw error
    }
}
