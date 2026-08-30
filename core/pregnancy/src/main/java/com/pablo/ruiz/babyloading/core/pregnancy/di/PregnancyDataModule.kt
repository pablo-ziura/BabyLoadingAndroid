package com.pablo.ruiz.babyloading.core.pregnancy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import com.pablo.ruiz.babyloading.core.pregnancy.data.DataStorePregnancyPreferencesDataSource
import com.pablo.ruiz.babyloading.core.pregnancy.data.DefaultPregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.data.PregnancyPreferencesDataSource
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PregnancyRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPregnancyRepository(
        repository: DefaultPregnancyRepository,
    ): PregnancyRepository

    @Binds
    @Singleton
    abstract fun bindPregnancyPreferencesDataSource(
        dataSource: DataStorePregnancyPreferencesDataSource,
    ): PregnancyPreferencesDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object PregnancyDataStoreModule {
    @Provides
    @Singleton
    fun providePregnancyDataStore(
        @ApplicationContext context: Context,
        storageConfig: AppStorageConfig,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = {
            context.preferencesDataStoreFile(storageConfig.pregnancyPreferences)
        },
    )
}
