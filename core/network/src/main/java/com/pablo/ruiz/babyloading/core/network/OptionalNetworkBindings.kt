package com.pablo.ruiz.babyloading.core.network

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OptionalNetworkBindings {
    @BindsOptionalOf
    abstract fun bindOptionalAccessTokenDataSource(): AccessTokenDataSource

    @BindsOptionalOf
    abstract fun bindOptionalAccessTokenRefreshDataSource(): AccessTokenRefreshDataSource
}
