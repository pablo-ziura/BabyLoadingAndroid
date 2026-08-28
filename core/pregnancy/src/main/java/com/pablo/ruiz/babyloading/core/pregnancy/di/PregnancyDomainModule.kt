package com.pablo.ruiz.babyloading.core.pregnancy.di

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDateValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PregnancyDomainModule {
    @Provides
    @Singleton
    fun providePregnancyCalculator(): PregnancyCalculator = PregnancyCalculator()

    @Provides
    @Singleton
    fun providePregnancyDateValidator(): PregnancyDateValidator = PregnancyDateValidator()
}
