package com.pablo.ruiz.babyloading.core.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStorageNamesTest {
    @Test
    fun productionScopePreservesExistingStorageNames() {
        assertEquals(
            AppStorageScope(
                pregnancyPreferences = "pregnancy_preferences",
                trackingPreferences = "tracking_preferences",
                galleryDatabase = "baby-loading.db",
                privateGalleryDirectory = "gallery",
                mediaStoreDirectory = "Baby Loading",
                mediaStoreFilePrefix = "BabyLoading",
                widgetDailyRefreshAction =
                    "com.pablo.ruiz.babyloading.feature.widget.action.DAILY_REFRESH",
            ),
            AppStorageNames.forApplicationId(AppStorageNames.ProductionApplicationId),
        )
    }

    @Test
    fun labScopeUsesDedicatedStorageAndWidgetIdentifiers() {
        assertEquals(
            AppStorageScope(
                pregnancyPreferences = "pregnancy_preferences-lab",
                trackingPreferences = "tracking_preferences-lab",
                galleryDatabase = "baby-loading-lab.db",
                privateGalleryDirectory = "gallery-lab",
                mediaStoreDirectory = "Baby Loading Lab",
                mediaStoreFilePrefix = "BabyLoadingLab",
                widgetDailyRefreshAction =
                    "com.pablo.ruiz.babyloading.lab.feature.widget.action.DAILY_REFRESH",
            ),
            AppStorageNames.forApplicationId(AppStorageNames.LabApplicationId),
        )
    }
}
