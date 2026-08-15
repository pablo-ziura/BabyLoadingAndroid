package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGalleryRepository()

    @Test
    fun itemsAreObservedAndCanBeSelected() = runTest {
        val viewModel = GalleryViewModel(repository)
        repository.itemsState.value = listOf(galleryItem("one"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.items.size)

        viewModel.onEvent(GalleryEvent.ItemSelected("one"))
        assertEquals("one", viewModel.uiState.value.selectedItem?.id)
    }

    @Test
    fun partialImportProducesSpecificMessage() = runTest {
        repository.nextImportResult = GalleryImportResult(importedCount = 2, failedCount = 1)
        val viewModel = GalleryViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(GalleryEvent.PhotosSelected(listOf("one", "two", "bad")))
        advanceUntilIdle()

        assertEquals(
            GalleryUserMessage.ImportPartiallyCompleted(2, 1),
            viewModel.uiState.value.message,
        )
        assertFalse(viewModel.uiState.value.isImporting)
    }

    @Test
    fun confirmedDeletionUsesPendingItemAndClosesViewer() = runTest {
        val item = galleryItem("one")
        repository.itemsState.value = listOf(item)
        val viewModel = GalleryViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(GalleryEvent.ItemSelected(item.id))
        viewModel.onEvent(GalleryEvent.DeleteRequested(item.id))
        assertEquals(item.id, viewModel.uiState.value.pendingDeleteItem?.id)

        viewModel.onEvent(GalleryEvent.DeleteConfirmed)
        advanceUntilIdle()

        assertEquals(listOf(item.id), repository.deletedIds)
        assertNull(viewModel.uiState.value.pendingDeleteItem)
        assertNull(viewModel.uiState.value.selectedItem)
    }

    private fun galleryItem(id: String) = GalleryItem(
        id = id,
        privateFilePath = "/private/$id.jpg",
        capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
        source = GallerySource.Imported,
    )

    private class FakeGalleryRepository : GalleryRepository {
        val itemsState = MutableStateFlow<List<GalleryItem>>(emptyList())
        override val items: Flow<List<GalleryItem>> = itemsState
        var nextImportResult = GalleryImportResult(0, 0)
        val deletedIds = mutableListOf<String>()

        override suspend fun importPhotos(sourceUris: List<String>): GalleryImportResult {
            return nextImportResult
        }

        override suspend fun addPrivatePhoto(
            data: ByteArray,
            source: GallerySource,
            capturedAt: Instant,
            pregnancyWeek: Int?,
        ): GalleryItem = error("Not used")

        override suspend fun deleteItem(id: String) {
            deletedIds += id
            itemsState.value = itemsState.value.filterNot { it.id == id }
        }
    }
}
