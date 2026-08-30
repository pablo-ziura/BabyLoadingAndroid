package com.pablo.ruiz.babyloading.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AppStorageConfigFactoryTest {
    private val factory = AppStorageConfigFactory()

    @Test
    fun `production uses existing storage contracts`() {
        assertEquals(
            AppStorageConfig(
                pregnancyPreferences = "pregnancy_preferences",
                trackingPreferences = "tracking_preferences",
                galleryDatabase = "baby-loading.db",
                privateGalleryDirectory = "gallery",
                mediaStoreDirectory = "Baby Loading",
                mediaStoreFilePrefix = "BabyLoading",
                widgetDailyRefreshAction =
                    "com.pablo.ruiz.babyloading.feature.widget.action.DAILY_REFRESH",
            ),
            factory.forApplicationId(AppStorageConfigFactory.ProductionApplicationId),
        )
    }

    @Test
    fun `lab uses isolated storage contracts`() {
        assertEquals(
            AppStorageConfig(
                pregnancyPreferences = "pregnancy_preferences-lab",
                trackingPreferences = "tracking_preferences-lab",
                galleryDatabase = "baby-loading-lab.db",
                privateGalleryDirectory = "gallery-lab",
                mediaStoreDirectory = "Baby Loading Lab",
                mediaStoreFilePrefix = "BabyLoadingLab",
                widgetDailyRefreshAction =
                    "com.pablo.ruiz.babyloading.lab.feature.widget.action.DAILY_REFRESH",
            ),
            factory.forApplicationId(AppStorageConfigFactory.LabApplicationId),
        )
    }

    @Test
    fun `unsupported application id fails explicitly`() {
        assertThrows(IllegalStateException::class.java) {
            factory.forApplicationId("com.example.unsupported")
        }
    }
}
