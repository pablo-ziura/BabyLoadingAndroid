package com.example.babyloading.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    @UnauthenticatedClient
    fun provideUnauthenticatedClient(
        networkConfiguration: NetworkConfiguration,
        defaultHeadersInterceptor: DefaultHeadersInterceptor,
        @NetworkInterceptors networkInterceptors: Set<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(
            networkConfiguration.connectTimeout.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        )
        .readTimeout(
            networkConfiguration.readTimeout.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        )
        .writeTimeout(
            networkConfiguration.writeTimeout.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        )
        .callTimeout(
            networkConfiguration.callTimeout.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        )
        .addInterceptor(defaultHeadersInterceptor)
        .apply {
            networkInterceptors.forEach { addInterceptor(it) }
        }
        .build()

    @Provides
    @Singleton
    fun provideAuthenticatedClient(
        @UnauthenticatedClient unauthenticatedClient: OkHttpClient,
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
    ): OkHttpClient = unauthenticatedClient.newBuilder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenRefreshAuthenticator)
        .build()

    @Provides
    @Singleton
    @UnauthenticatedRetrofit
    fun provideUnauthenticatedRetrofit(
        networkConfiguration: NetworkConfiguration,
        json: Json,
        @UnauthenticatedClient unauthenticatedClient: OkHttpClient,
    ): Retrofit = createRetrofit(
        networkConfiguration = networkConfiguration,
        json = json,
        okHttpClient = unauthenticatedClient,
    )

    @Provides
    @Singleton
    fun provideAuthenticatedRetrofit(
        networkConfiguration: NetworkConfiguration,
        json: Json,
        authenticatedClient: OkHttpClient,
    ): Retrofit = createRetrofit(
        networkConfiguration = networkConfiguration,
        json = json,
        okHttpClient = authenticatedClient,
    )

    private fun createRetrofit(
        networkConfiguration: NetworkConfiguration,
        json: Json,
        okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(networkConfiguration.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()

    private const val JSON_MEDIA_TYPE = "application/json"
}
