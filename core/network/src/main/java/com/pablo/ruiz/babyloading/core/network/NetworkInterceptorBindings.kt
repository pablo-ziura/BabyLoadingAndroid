package com.pablo.ruiz.babyloading.core.network

import dagger.Module
import dagger.multibindings.Multibinds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkInterceptorBindings {
    @Multibinds
    @NetworkInterceptors
    internal abstract fun bindNetworkInterceptors(): Set<Interceptor>
}
