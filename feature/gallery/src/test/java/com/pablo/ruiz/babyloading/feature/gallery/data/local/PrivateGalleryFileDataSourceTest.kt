package com.pablo.ruiz.babyloading.feature.gallery.data.local

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class PrivateGalleryFileDataSourceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

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

    @Test
    fun temporaryCaptureIsCopiedWithBoundedMemoryAndThenDeleted() {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val sourceFile = File(cacheDirectory, "guided-capture-valid.jpg").apply {
            outputStream().use { it.write(byteArrayOf(1, 2, 3)) }
        }
        val targetFile = File(temporaryFolder.newFolder("gallery"), "stored.jpg")

        storeTemporaryCapture(sourceFile, targetFile, cacheDirectory, maximumBytes = 3)

        assertArrayEquals(byteArrayOf(1, 2, 3), targetFile.inputStream().use { it.readBytes() })
        org.junit.Assert.assertFalse(sourceFile.exists())
    }

    @Test
    fun oversizedCaptureRemovesBothTemporaryAndPartialFiles() {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val sourceFile = File(cacheDirectory, "guided-capture-large.jpg").apply {
            outputStream().use { it.write(ByteArray(4)) }
        }
        val targetFile = File(temporaryFolder.newFolder("gallery"), "stored.jpg")

        assertThrows(IOException::class.java) {
            storeTemporaryCapture(sourceFile, targetFile, cacheDirectory, maximumBytes = 3)
        }

        org.junit.Assert.assertFalse(sourceFile.exists())
        org.junit.Assert.assertFalse(targetFile.exists())
    }

    @Test
    fun emptyCaptureRemovesBothTemporaryAndPartialFiles() {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val sourceFile = File(cacheDirectory, "guided-capture-empty.jpg").apply {
            createNewFile()
        }
        val targetFile = File(temporaryFolder.newFolder("gallery"), "stored.jpg")

        assertThrows(IllegalArgumentException::class.java) {
            storeTemporaryCapture(sourceFile, targetFile, cacheDirectory)
        }

        org.junit.Assert.assertFalse(sourceFile.exists())
        org.junit.Assert.assertFalse(targetFile.exists())
    }

    @Test
    fun temporaryCaptureOutsideTheCacheDirectoryIsRejected() {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val sourceFile = File(temporaryFolder.root, "guided-capture-invalid.jpg").apply {
            outputStream().use { it.write(1) }
        }

        assertThrows(IllegalArgumentException::class.java) {
            requireTemporaryCaptureFile(sourceFile, cacheDirectory)
        }
    }
}
