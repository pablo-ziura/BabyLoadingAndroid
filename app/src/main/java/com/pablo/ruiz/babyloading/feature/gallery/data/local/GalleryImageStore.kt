package com.pablo.ruiz.babyloading.feature.gallery.data.local

import java.io.File

interface GalleryImageStore {
    suspend fun importFromUri(uriValue: String): StoredGalleryImage

    suspend fun writeJpeg(data: ByteArray): StoredGalleryImage

    fun fileFor(fileName: String): File

    suspend fun delete(fileName: String)
}

data class StoredGalleryImage(
    val fileName: String,
    val file: File,
)
