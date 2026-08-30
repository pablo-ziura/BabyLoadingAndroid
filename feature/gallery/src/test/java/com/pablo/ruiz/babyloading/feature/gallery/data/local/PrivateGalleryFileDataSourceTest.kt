package com.pablo.ruiz.babyloading.feature.gallery.data.local

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PrivateGalleryFileDataSourceTest {
    @Test
    fun supportedImageMimeTypesKeepTheirExpectedExtensions() {
        assertEquals("jpg", galleryFileExtension("image/jpeg"))
        assertEquals("png", galleryFileExtension("image/png"))
        assertEquals("webp", galleryFileExtension("image/webp"))
        assertEquals("heic", galleryFileExtension("image/heif"))
        assertEquals("jpg", galleryFileExtension(null))
        assertThrows(IOException::class.java) {
            galleryFileExtension("text/plain")
        }
    }

    @Test
    fun privateImageLimitRemainsTwentyFiveMebibytes() {
        assertEquals(25 * 1024 * 1024, MaximumPrivateGalleryImageBytes)
    }

    @Test
    fun copyAcceptsTheLimitAndRejectsTheNextByte() {
        val accepted = ByteArray(8) { it.toByte() }
        val output = ByteArrayOutputStream()

        copyGalleryImageWithLimit(ByteArrayInputStream(accepted), output, maximumBytes = 8)
        assertArrayEquals(accepted, output.toByteArray())

        assertThrows(IOException::class.java) {
            copyGalleryImageWithLimit(
                input = ByteArrayInputStream(ByteArray(9)),
                output = ByteArrayOutputStream(),
                maximumBytes = 8,
            )
        }
    }
}
