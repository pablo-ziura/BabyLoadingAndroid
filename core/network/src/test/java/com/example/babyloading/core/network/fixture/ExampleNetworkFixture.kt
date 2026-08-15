package com.example.babyloading.core.network.fixture

import com.example.babyloading.core.network.NetworkResult
import com.example.babyloading.core.network.map
import com.example.babyloading.core.network.safeApiCall
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class ExampleItemDto(
    val id: Long,
    val title: String,
)

@Serializable
data class ExampleItemCreateRequest(
    val title: String,
)

data class ExampleItem(
    val id: Long,
    val title: String,
)

interface ExampleApiService {
    @GET("items")
    suspend fun getItems(): Response<List<ExampleItemDto>>

    @POST("items")
    suspend fun createItem(
        @Body request: ExampleItemCreateRequest,
    ): Response<ExampleItemDto>
}

class ExampleRemoteDataSource(
    private val apiService: ExampleApiService,
) {
    suspend fun getItems(): NetworkResult<List<ExampleItemDto>> =
        safeApiCall(apiService::getItems)
}

class ExampleRepository(
    private val remoteDataSource: ExampleRemoteDataSource,
) {
    fun observeItems(): Flow<NetworkResult<List<ExampleItem>>> = flow {
        emit(
            remoteDataSource.getItems().map { items ->
                items.map { item ->
                    ExampleItem(
                        id = item.id,
                        title = item.title,
                    )
                }
            },
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ExampleApiModule {
    @Provides
    fun provideExampleApiService(retrofit: Retrofit): ExampleApiService = retrofit.create()
}
