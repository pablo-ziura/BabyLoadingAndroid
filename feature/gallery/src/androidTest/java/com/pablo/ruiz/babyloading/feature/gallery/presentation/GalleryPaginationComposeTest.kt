package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class GalleryPaginationComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onlyTheSelectedPhotoPageIsComposed() {
        val items = (1..8).map { index ->
            GalleryItem(
                id = index.toString(),
                privateFilePath = "/missing/$index.jpg",
                capturedAt = Instant.EPOCH,
                source = GallerySource.Imported,
            )
        }
        composeRule.setContent {
            BabyLoadingTheme {
                UltrasoundGallerySection(
                    items = items,
                    isImporting = false,
                    onAddPhotos = {},
                    onItemSelected = {},
                    onDeleteRequested = {},
                )
            }
        }

        composeRule.onNodeWithTag("ultrasound-photo-1").assertExists()
        composeRule.onNodeWithTag("ultrasound-photo-7").assertExists()
        composeRule.onNodeWithTag("ultrasound-photo-8").assertDoesNotExist()
        composeRule.onNodeWithTag(NEXT_PAGE_TEST_TAG).performClick()
        composeRule.onNodeWithTag("ultrasound-photo-1").assertDoesNotExist()
        composeRule.onNodeWithTag("ultrasound-photo-8").assertExists()
        composeRule.onNodeWithTag(ADD_PHOTO_TEST_TAG).assertExists()
    }
}
