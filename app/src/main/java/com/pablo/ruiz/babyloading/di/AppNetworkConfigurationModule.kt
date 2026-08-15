package com.pablo.ruiz.babyloading.di

import com.pablo.ruiz.babyloading.BuildConfig
import com.pablo.ruiz.babyloading.core.network.NetworkConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkConfigurationModule {

    @Provides
    @Singleton
    fun provideNetworkConfiguration(): NetworkConfiguration =
        NetworkConfiguration.create(BuildConfig.API_BASE_URL)
}
