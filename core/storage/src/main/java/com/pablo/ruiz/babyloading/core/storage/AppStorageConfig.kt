package com.pablo.ruiz.babyloading.core.storage

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

data class AppStorageConfig(
    val pregnancyPreferences: String,
    val trackingPreferences: String,
    val galleryDatabase: String,
    val privateGalleryDirectory: String,
    val mediaStoreDirectory: String,
    val mediaStoreFilePrefix: String,
    val widgetDailyRefreshAction: String,
)

class AppStorageConfigFactory @Inject constructor() {
    fun forApplicationId(applicationId: String): AppStorageConfig {
        val suffix = when (applicationId) {
            ProductionApplicationId -> ""
            LabApplicationId -> "-lab"
            else -> error("Unsupported application ID for Baby Loading storage: $applicationId")
        }
        return AppStorageConfig(
            pregnancyPreferences = "pregnancy_preferences$suffix",
            trackingPreferences = "tracking_preferences$suffix",
            galleryDatabase = "baby-loading$suffix.db",
            privateGalleryDirectory = "gallery$suffix",
            mediaStoreDirectory = "Baby Loading${if (suffix.isEmpty()) "" else " Lab"}",
            mediaStoreFilePrefix = "BabyLoading${if (suffix.isEmpty()) "" else "Lab"}",
            widgetDailyRefreshAction = "$applicationId.feature.widget.action.DAILY_REFRESH",
        )
    }

    companion object {
        const val ProductionApplicationId = "com.pablo.ruiz.babyloading"
        const val LabApplicationId = "$ProductionApplicationId.lab"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppStorageModule {
    @Provides
    @Singleton
    fun provideAppStorageConfig(
        @ApplicationContext context: Context,
        factory: AppStorageConfigFactory,
    ): AppStorageConfig = factory.forApplicationId(context.packageName)
}
