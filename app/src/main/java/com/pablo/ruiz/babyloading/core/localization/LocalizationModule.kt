package com.pablo.ruiz.babyloading.core.localization

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalizationModule {
    @Binds
    @Singleton
    abstract fun bindAppLanguageProvider(
        provider: AndroidAppLocaleProvider,
    ): AppLanguageProvider

    @Binds
    @Singleton
    abstract fun bindAppLanguageChanges(
        changes: AndroidAppLanguageChanges,
    ): AppLanguageChanges
}
