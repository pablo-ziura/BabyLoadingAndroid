package com.pablo.ruiz.babyloading.core.pregnancy.content.di

import com.pablo.ruiz.babyloading.core.pregnancy.content.data.BundledPregnancyContentSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.LocalPregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.data.PregnancyContentSource
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PregnancyContentModule {
    @Binds
    @Singleton
    abstract fun bindPregnancyContentSource(
        source: BundledPregnancyContentSource,
    ): PregnancyContentSource

    @Binds
    @Singleton
    abstract fun bindPregnancyContentRepository(
        repository: LocalPregnancyContentRepository,
    ): PregnancyContentRepository
}
