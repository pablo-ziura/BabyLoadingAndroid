package com.pablo.ruiz.babyloading.core.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class NetworkInterceptors
