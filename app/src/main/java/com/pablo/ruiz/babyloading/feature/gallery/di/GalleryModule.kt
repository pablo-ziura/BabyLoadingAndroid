package com.pablo.ruiz.babyloading.feature.gallery.di

import android.content.Context
import androidx.room.Room
import com.pablo.ruiz.babyloading.feature.gallery.data.OfflineGalleryRepository
import com.pablo.ruiz.babyloading.feature.gallery.data.local.BabyLoadingDatabase
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryDao
import com.pablo.ruiz.babyloading.feature.gallery.data.local.GalleryImageStore
import com.pablo.ruiz.babyloading.feature.gallery.data.local.PrivateGalleryImageStore
import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
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
    abstract fun bindGalleryImageStore(
        imageStore: PrivateGalleryImageStore,
    ): GalleryImageStore
}

@Module
@InstallIn(SingletonComponent::class)
object GalleryDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): BabyLoadingDatabase {
        return Room.databaseBuilder(
            context,
            BabyLoadingDatabase::class.java,
            "baby-loading.db",
        ).build()
    }

    @Provides
    fun provideGalleryDao(database: BabyLoadingDatabase): GalleryDao = database.galleryDao()
}
