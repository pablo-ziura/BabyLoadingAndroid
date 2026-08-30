package com.pablo.ruiz.babyloading.feature.gallery.presentation

import android.graphics.ImageDecoder
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GalleryBitmapDecoder {
    suspend fun load(
        filePath: String,
        requestedSizePx: Int,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.isFile) return@withContext null

        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, imageInfo, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.memorySizePolicy = ImageDecoder.MEMORY_POLICY_LOW_RAM
                decoder.setTargetSampleSize(
                    calculateInSampleSize(
                        width = imageInfo.size.width,
                        height = imageInfo.size.height,
                        requestedSize = requestedSizePx,
                    ),
                )
            }.asImageBitmap()
        }
            .getOrNull()
    }

    fun calculateInSampleSize(
        width: Int,
        height: Int,
        requestedSize: Int,
    ): Int {
        if (requestedSize <= 0) return 1
        var sampleSize = 1
        while (width / (sampleSize * 2) >= requestedSize &&
            height / (sampleSize * 2) >= requestedSize
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
