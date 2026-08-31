package com.pablo.ruiz.babyloading.feature.gallery.data.local

import java.io.File
import java.io.IOException

internal class GalleryFilePathResolver(
    rootDirectory: File,
    directoryName: String,
) {
    private val galleryDirectory = File(rootDirectory, directoryName)

    fun fileFor(fileName: String): File {
        require(fileName == File(fileName).name) { "Gallery file name must not contain a path" }
        return File(galleryDirectory, fileName)
    }

    fun ensureDirectory(): File {
        if (!galleryDirectory.isDirectory && !galleryDirectory.mkdirs()) {
            throw IOException("Unable to create the private gallery directory")
        }
        return galleryDirectory
    }
}
