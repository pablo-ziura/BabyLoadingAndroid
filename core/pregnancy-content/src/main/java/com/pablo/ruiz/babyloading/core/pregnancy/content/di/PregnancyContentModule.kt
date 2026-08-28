package com.pablo.ruiz.babyloading.core.pregnancy.content.di

import com.pablo.ruiz.babyloading.core.pregnancy.content.data.BundledPregnancyContentDataSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.LocalPregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentDataSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentJson
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
abstract class PregnancyContentModule {
    @Binds
    @Singleton
    abstract fun bindPregnancyContentDataSource(
        dataSource: BundledPregnancyContentDataSource,
    ): PregnancyContentDataSource

    @Binds
    @Singleton
    abstract fun bindPregnancyContentRepository(
        repository: LocalPregnancyContentRepository,
    ): PregnancyContentRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PregnancyContentJsonModule {
    @Provides
    @Singleton
    @PregnancyContentJson
    fun providePregnancyContentJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}
