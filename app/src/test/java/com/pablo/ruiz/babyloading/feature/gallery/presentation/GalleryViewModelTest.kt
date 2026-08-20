package com.pablo.ruiz.babyloading.feature.gallery.presentation

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingStatus
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
    private val trackingPreferencesRepository = FakeTrackingPreferencesRepository()
    private val clock = Clock.fixed(
        Instant.parse("2026-08-18T12:00:00Z"),
        ZoneOffset.UTC,
    )

    @Test
    fun sourceSpecificItemsAreObservedAndCanBeSelected() = runTest {
        val viewModel = createViewModel()
        repository.importedItemsState.value = listOf(galleryItem("imported"))
        repository.trackingItemsState.value = listOf(
            galleryItem("tracking", source = GallerySource.GuidedTracking),
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("imported"), viewModel.uiState.value.importedItems.map { it.id })
        assertEquals(listOf("tracking"), viewModel.uiState.value.trackingItems.map { it.id })

        viewModel.onEvent(GalleryEvent.ItemSelected("tracking"))
        assertEquals("tracking", viewModel.uiState.value.selectedItem?.id)
    }

    @Test
    fun partialImportProducesSpecificMessage() = runTest {
        repository.nextImportResult = GalleryImportResult(importedCount = 2, failedCount = 1)
        val viewModel = createViewModel()
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
        repository.importedItemsState.value = listOf(item)
        val viewModel = createViewModel()
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

    @Test
    fun cadenceUpdatesTrackingStatusFromOnlyGuidedCaptures() = runTest {
        repository.importedItemsState.value = listOf(galleryItem("imported"))
        repository.trackingItemsState.value = listOf(
            galleryItem(
                id = "tracking",
                source = GallerySource.GuidedTracking,
                capturedAt = Instant.parse("2026-08-10T12:00:00Z"),
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            TrackingStatus.Pending(java.time.LocalDate.parse("2026-08-17")),
            viewModel.uiState.value.trackingStatus,
        )

        viewModel.onEvent(GalleryEvent.TrackingCadenceSelected(TrackingCadence.EveryTwoWeeks))
        advanceUntilIdle()

        assertEquals(TrackingCadence.EveryTwoWeeks, viewModel.uiState.value.trackingCadence)
        assertEquals(
            TrackingStatus.UpToDate(java.time.LocalDate.parse("2026-08-24")),
            viewModel.uiState.value.trackingStatus,
        )
    }

    private fun createViewModel() = GalleryViewModel(
        repository = repository,
        trackingPreferencesRepository = trackingPreferencesRepository,
        clock = clock,
    )

    private fun galleryItem(
        id: String,
        source: GallerySource = GallerySource.Imported,
        capturedAt: Instant = Instant.parse("2026-08-15T12:00:00Z"),
    ) = GalleryItem(
        id = id,
        privateFilePath = "/private/$id.jpg",
        capturedAt = capturedAt,
        source = source,
    )

    private class FakeGalleryRepository : GalleryRepository {
        val importedItemsState = MutableStateFlow<List<GalleryItem>>(emptyList())
        val trackingItemsState = MutableStateFlow<List<GalleryItem>>(emptyList())
        override val importedItems: Flow<List<GalleryItem>> = importedItemsState
        override val trackingItems: Flow<List<GalleryItem>> = trackingItemsState
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
            importedItemsState.value = importedItemsState.value.filterNot { it.id == id }
            trackingItemsState.value = trackingItemsState.value.filterNot { it.id == id }
        }
    }

    private class FakeTrackingPreferencesRepository : TrackingPreferencesRepository {
        private val cadenceState = MutableStateFlow(TrackingCadence.Default)
        override val cadence: Flow<TrackingCadence> = cadenceState

        override suspend fun setCadence(cadence: TrackingCadence) {
            cadenceState.value = cadence
        }
    }
}
