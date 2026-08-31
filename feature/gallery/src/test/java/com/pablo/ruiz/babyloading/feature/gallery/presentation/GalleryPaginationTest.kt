package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryPaginationTest {
    @Test
    fun emptyAndSinglePageCollectionsStayOnTheFirstPage() {
        assertEquals(GalleryPage(emptyList(), 0, 1), galleryPage(emptyList(), 0))

        val sevenItems = galleryItems(7)
        assertEquals(GalleryPage(sevenItems, 0, 1), galleryPage(sevenItems, 0))
    }

    @Test
    fun eighthPhotoStartsASecondPage() {
        val items = galleryItems(8)

        val firstPage = galleryPage(items, 0)
        val secondPage = galleryPage(items, 1)

        assertEquals(items.take(7), firstPage.items)
        assertEquals(items.takeLast(1), secondPage.items)
        assertEquals(2, secondPage.pageCount)
    }

    @Test
    fun deletingTheLastPageClampsTheRequestedIndex() {
        val page = galleryPage(galleryItems(7), requestedPageIndex = 1)

        assertEquals(0, page.pageIndex)
        assertEquals(1, page.pageCount)
    }

    private fun galleryItems(count: Int): List<GalleryItem> {
        return (1..count).map { index ->
            GalleryItem(
                id = index.toString(),
                privateFilePath = "/private/$index.jpg",
                capturedAt = Instant.EPOCH,
                source = GallerySource.Imported,
            )
        }
    }
}
