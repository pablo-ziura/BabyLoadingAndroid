package com.pablo.ruiz.babyloading.feature.gallery.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GalleryFilePathResolverTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun resolvingAFileDoesNotCreateTheGalleryDirectory() {
        val resolver = GalleryFilePathResolver(temporaryFolder.root, "gallery-test")

        val file = resolver.fileFor("photo.jpg")

        assertEquals("photo.jpg", file.name)
        assertFalse(file.parentFile?.exists() == true)
    }

    @Test
    fun directoryCreationIsExplicitAndRejectsPathsInFileNames() {
        val resolver = GalleryFilePathResolver(temporaryFolder.root, "gallery-test")

        assertTrue(resolver.ensureDirectory().isDirectory)
        assertThrows(IllegalArgumentException::class.java) {
            resolver.fileFor("nested/photo.jpg")
        }
    }
}
