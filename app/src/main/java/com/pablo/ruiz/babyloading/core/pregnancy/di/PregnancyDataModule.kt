package com.pablo.ruiz.babyloading.core.pregnancy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pablo.ruiz.babyloading.core.storage.AppStorageNames
import com.pablo.ruiz.babyloading.core.pregnancy.data.DataStorePregnancyRepository
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.pregnancyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppStorageNames.current.pregnancyPreferences,
)

@Module
@InstallIn(SingletonComponent::class)
abstract class PregnancyRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPregnancyRepository(
        repository: DataStorePregnancyRepository,
    ): PregnancyRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PregnancyDataStoreModule {
    @Provides
    @Singleton
    fun providePregnancyDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.pregnancyDataStore
}
