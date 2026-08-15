package com.pablo.ruiz.babyloading.feature.tracking.di

import com.pablo.ruiz.babyloading.feature.tracking.data.MediaStoreTrackingPhotoExporter
import com.pablo.ruiz.babyloading.feature.tracking.domain.repository.TrackingPhotoExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {
    @Binds
    @Singleton
    abstract fun bindTrackingPhotoExporter(
        exporter: MediaStoreTrackingPhotoExporter,
    ): TrackingPhotoExporter
}
