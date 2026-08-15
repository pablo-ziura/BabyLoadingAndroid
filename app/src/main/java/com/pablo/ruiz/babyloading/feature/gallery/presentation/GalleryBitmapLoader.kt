package com.pablo.ruiz.babyloading.feature.gallery.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GalleryBitmapLoader {
    suspend fun load(
        filePath: String,
        requestedSizePx: Int,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.isFile) return@withContext null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                requestedSize = requestedSizePx,
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
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
