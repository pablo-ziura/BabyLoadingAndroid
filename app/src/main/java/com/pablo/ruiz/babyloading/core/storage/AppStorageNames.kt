package com.pablo.ruiz.babyloading.core.storage

import com.pablo.ruiz.babyloading.BuildConfig

internal object AppStorageNames {
    const val ProductionApplicationId = "com.pablo.ruiz.babyloading"
    const val LabApplicationId = "$ProductionApplicationId.lab"

    val current: AppStorageScope
        get() = forApplicationId(BuildConfig.APPLICATION_ID)

    fun forApplicationId(applicationId: String): AppStorageScope {
        val suffix = when (applicationId) {
            ProductionApplicationId -> ""
            LabApplicationId -> "-lab"
            else -> error("Unsupported application ID for Baby Loading storage: $applicationId")
        }
        return AppStorageScope(
            pregnancyPreferences = "pregnancy_preferences$suffix",
            trackingPreferences = "tracking_preferences$suffix",
            galleryDatabase = "baby-loading$suffix.db",
            privateGalleryDirectory = "gallery$suffix",
            mediaStoreDirectory = "Baby Loading${if (suffix.isEmpty()) "" else " Lab"}",
            mediaStoreFilePrefix = "BabyLoading${if (suffix.isEmpty()) "" else "Lab"}",
            widgetDailyRefreshAction = "$applicationId.feature.widget.action.DAILY_REFRESH",
        )
    }
}

internal data class AppStorageScope(
    val pregnancyPreferences: String,
    val trackingPreferences: String,
    val galleryDatabase: String,
    val privateGalleryDirectory: String,
    val mediaStoreDirectory: String,
    val mediaStoreFilePrefix: String,
    val widgetDailyRefreshAction: String,
)
