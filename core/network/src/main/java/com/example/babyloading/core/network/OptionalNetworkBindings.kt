package com.example.babyloading.core.network

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OptionalNetworkBindings {
    @BindsOptionalOf
    abstract fun bindOptionalAccessTokenStore(): AccessTokenStore

    @BindsOptionalOf
    abstract fun bindOptionalAccessTokenRefresher(): AccessTokenRefresher
}
