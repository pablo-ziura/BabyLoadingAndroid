package com.example.babyloading.di

import com.example.babyloading.BuildConfig
import com.example.babyloading.core.network.NetworkConfiguration
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
