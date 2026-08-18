package com.pablo.ruiz.babyloading.feature.tracking.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.usecase.CalculatePregnancyProgressUseCase
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryImportResult
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import com.pablo.ruiz.babyloading.feature.tracking.domain.usecase.SaveGuidedTrackingPhotoUseCase
import com.pablo.ruiz.babyloading.test.MainDispatcherRule
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuidedTrackingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-08-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val pregnancyRepository = FakePregnancyRepository(LocalDate.parse("2026-03-01"))
    private val galleryRepository = FakeGalleryRepository()
    private val exporter = FakeExporter()

    @Test
    fun captureUsesCurrentPregnancyWeekAndReportsBothCopies() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(GuidedTrackingEvent.PhotoCaptured(byteArrayOf(1, 2)))
        advanceUntilIdle()

        assertEquals(23, viewModel.uiState.value.pregnancyWeek)
        assertEquals(23, galleryRepository.savedWeek)
        assertEquals(GuidedTrackingSaveOutcome.PrivateAndPublic, viewModel.uiState.value.saveOutcome)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun exportFailureReportsPrivateOnlyOutcome() = runTest {
        exporter.failure = IOException("MediaStore unavailable")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(GuidedTrackingEvent.PhotoCaptured(byteArrayOf(1)))
        advanceUntilIdle()

        assertEquals(GuidedTrackingSaveOutcome.PrivateOnly, viewModel.uiState.value.saveOutcome)
    }

    @Test
    fun privateSaveFailureCanBeDismissed() = runTest {
        galleryRepository.failure = IOException("Private storage unavailable")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(GuidedTrackingEvent.PhotoCaptured(byteArrayOf(1)))
        advanceUntilIdle()

        assertEquals(GuidedTrackingError.SaveFailed, viewModel.uiState.value.error)
        viewModel.onEvent(GuidedTrackingEvent.ErrorShown)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun latestGuidedPhotoBecomesCameraReference() = runTest {
        galleryRepository.itemsState.value = listOf(
            GalleryItem(
                id = "imported",
                privateFilePath = "/imported.jpg",
                capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
                source = GallerySource.Imported,
            ),
            GalleryItem(
                id = "older-guided",
                privateFilePath = "/older-guided.jpg",
                capturedAt = Instant.parse("2026-08-10T12:00:00Z"),
                source = GallerySource.GuidedTracking,
            ),
            GalleryItem(
                id = "latest-guided",
                privateFilePath = "/latest-guided.jpg",
                capturedAt = Instant.parse("2026-08-14T12:00:00Z"),
                source = GallerySource.GuidedTracking,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("/latest-guided.jpg", viewModel.uiState.value.referenceImagePath)
    }

    private fun createViewModel(): GuidedTrackingViewModel {
        val calculateProgress = CalculatePregnancyProgressUseCase(PregnancyCalculator(), clock)
        return GuidedTrackingViewModel(
            pregnancyRepository = pregnancyRepository,
            galleryRepository = galleryRepository,
            calculateProgress = calculateProgress,
            savePhoto = SaveGuidedTrackingPhotoUseCase(galleryRepository, exporter, clock),
        )
    }

    private class FakePregnancyRepository(date: LocalDate?) : PregnancyRepository {
        override val lastPeriodDate = MutableStateFlow(date)
        override suspend fun setLastPeriodDate(date: LocalDate) {
            lastPeriodDate.value = date
        }
        override suspend fun clearLastPeriodDate() {
            lastPeriodDate.value = null
        }
    }

    private class FakeGalleryRepository : GalleryRepository {
        val itemsState = MutableStateFlow<List<GalleryItem>>(emptyList())
        override val items: Flow<List<GalleryItem>> = itemsState
        var savedWeek: Int? = null
        var failure: Throwable? = null

        override suspend fun importPhotos(sourceUris: List<String>) = GalleryImportResult(0, 0)

        override suspend fun addPrivatePhoto(
            data: ByteArray,
            source: GallerySource,
            capturedAt: Instant,
            pregnancyWeek: Int?,
        ): GalleryItem {
            failure?.let { throw it }
            savedWeek = pregnancyWeek
            return GalleryItem("saved", "/saved.jpg", capturedAt, source, pregnancyWeek)
        }

        override suspend fun deleteItem(id: String) = Unit
    }

    private class FakeExporter : TrackingPhotoExporter {
        var failure: Throwable? = null
        override suspend fun exportJpeg(data: ByteArray, capturedAt: Instant): String {
            failure?.let { throw it }
            return "content://media/saved"
        }
    }
}
