package com.example.babyloading.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object DebugNetworkModule {
    @Provides
    @IntoSet
    @Singleton
    @NetworkInterceptors
    fun provideHttpLoggingInterceptor(): Interceptor = createHttpLoggingInterceptor()
}

internal fun createHttpLoggingInterceptor(
    logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT,
): HttpLoggingInterceptor = HttpLoggingInterceptor(logger).apply {
    level = HttpLoggingInterceptor.Level.BASIC
    REDACTED_HEADERS.forEach(::redactHeader)
}

private val REDACTED_HEADERS = setOf(
    AUTHORIZATION_HEADER,
    "Cookie",
    "Set-Cookie",
)
