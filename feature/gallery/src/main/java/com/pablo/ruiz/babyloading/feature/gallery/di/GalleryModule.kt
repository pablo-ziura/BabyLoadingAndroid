package com.pablo.ruiz.babyloading.feature.gallery.di

import android.content.Context
import androidx.room.Room
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import com.pablo.ruiz.babyloading.feature.gallery.data.DataStoreTrackingPreferencesDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.DefaultTrackingPreferencesRepository
import com.pablo.ruiz.babyloading.feature.gallery.data.OfflineGalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.data.TrackingPreferencesDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.BabyLoadingDatabase
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryDao
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryFileDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryRoomDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.PrivateGalleryFileDataSource
import com.pablo.ruiz.babyloading.feature.gallery.data.local.RoomGalleryDataSource
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.TrackingPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryBindingsModule {
    @Binds
    @Singleton
    abstract fun bindGalleryRepository(
        repository: OfflineGalleryRepository,
    ): GalleryRepository

    @Binds
    @Singleton
    abstract fun bindGalleryFileDataSource(
        dataSource: PrivateGalleryFileDataSource,
    ): GalleryFileDataSource

    @Binds
    @Singleton
    abstract fun bindGalleryRoomDataSource(
        dataSource: RoomGalleryDataSource,
    ): GalleryRoomDataSource

    @Binds
    @Singleton
    abstract fun bindTrackingPreferencesDataSource(
        dataSource: DataStoreTrackingPreferencesDataSource,
    ): TrackingPreferencesDataSource

    @Binds
    @Singleton
    abstract fun bindTrackingPreferencesRepository(
        repository: DefaultTrackingPreferencesRepository,
    ): TrackingPreferencesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object GalleryDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        storageConfig: AppStorageConfig,
    ): BabyLoadingDatabase {
        return Room.databaseBuilder(
            context,
            BabyLoadingDatabase::class.java,
            storageConfig.galleryDatabase,
        ).build()
    }

    @Provides
    fun provideGalleryDao(database: BabyLoadingDatabase): GalleryDao = database.galleryDao()
}
