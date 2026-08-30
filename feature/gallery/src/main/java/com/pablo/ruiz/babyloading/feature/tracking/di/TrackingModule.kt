package com.pablo.ruiz.babyloading.feature.tracking.di

import com.pablo.ruiz.babyloading.feature.tracking.data.AndroidTrackingMediaStoreGateway
import com.pablo.ruiz.babyloading.feature.tracking.data.DefaultTrackingPhotoExporter
import com.pablo.ruiz.babyloading.feature.tracking.data.MediaStoreTrackingPhotoDataSource
import com.pablo.ruiz.babyloading.feature.tracking.data.TrackingPhotoMediaStoreDataSource
import com.pablo.ruiz.babyloading.feature.tracking.data.TrackingMediaStoreGateway
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
    abstract fun bindTrackingMediaStoreGateway(
        gateway: AndroidTrackingMediaStoreGateway,
    ): TrackingMediaStoreGateway

    @Binds
    @Singleton
    abstract fun bindTrackingPhotoMediaStoreDataSource(
        dataSource: MediaStoreTrackingPhotoDataSource,
    ): TrackingPhotoMediaStoreDataSource

    @Binds
    @Singleton
    abstract fun bindTrackingPhotoExporter(
        exporter: DefaultTrackingPhotoExporter,
    ): TrackingPhotoExporter
}
