package com.pablo.ruiz.babyloading.core.network.fixture

import com.pablo.ruiz.babyloading.core.network.DefaultHeadersInterceptor
import com.pablo.ruiz.babyloading.core.network.NetworkConfiguration
import com.pablo.ruiz.babyloading.core.network.NetworkError
import com.pablo.ruiz.babyloading.core.network.NetworkModule
import com.pablo.ruiz.babyloading.core.network.NetworkResult
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var apiService: ExampleApiService
    private lateinit var repository: ExampleRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val configuration = NetworkConfiguration.create(server.url("/").toString())
        val retrofit = NetworkModule.provideUnauthenticatedRetrofit(
            networkConfiguration = configuration,
            json = NetworkModule.provideJson(),
            unauthenticatedClient = OkHttpClient.Builder()
                .addInterceptor(DefaultHeadersInterceptor())
                .build(),
        )
        apiService = ExampleApiModule.provideExampleApiService(retrofit)
        repository = ExampleRepository(ExampleRemoteDataSource(apiService))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun observeItemsMapsRemoteDtosAndIgnoresUnknownJsonFields() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body(
                    """
                    [
                      {"id": 1, "title": "First", "server_only": true},
                      {"id": 2, "title": "Second"}
                    ]
                    """.trimIndent(),
                )
                .build(),
        )

        val result = repository.observeItems().single()

        assertEquals(
            NetworkResult.Success(
                listOf(
                    ExampleItem(id = 1, title = "First"),
                    ExampleItem(id = 2, title = "Second"),
                ),
            ),
            result,
        )
        assertEquals("application/json", server.takeRequest().headers["Accept"])
    }

    @Test
    fun createItemSerializesRequestBodyAndSetsJsonHeaders() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .addHeader("Content-Type", "application/json")
                .body("""{"id":3,"title":"Created"}""")
                .build(),
        )

        val response = apiService.createItem(
            ExampleItemCreateRequest(title = "Created"),
        )
        val recordedRequest = server.takeRequest()

        assertTrue(response.isSuccessful)
        assertEquals("POST", recordedRequest.method)
        assertEquals("application/json", recordedRequest.headers["Accept"])
        assertEquals(
            "application/json",
            recordedRequest.headers["Content-Type"]?.substringBefore(';'),
        )
        assertEquals("""{"title":"Created"}""", recordedRequest.body?.utf8())
    }

    @Test
    fun observeItemsEmitsNetworkErrorWithoutThrowing() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(503)
                .body("""{"error":"unavailable"}""")
                .build(),
        )

        val result = repository.observeItems().single()

        assertTrue(result is NetworkResult.Error)
        val error = (result as NetworkResult.Error).networkError
        assertEquals(
            NetworkError.ServerError(
                statusCode = 503,
                bodyPreview = """{"error":"unavailable"}""",
            ),
            error,
        )
    }

    @Test
    fun observeItemsIsColdAndExecutesOncePerCollector() = runTest {
        repeat(2) {
            server.enqueue(
                MockResponse.Builder()
                    .addHeader("Content-Type", "application/json")
                    .body("[]")
                    .build(),
            )
        }

        repository.observeItems().single()
        repository.observeItems().single()

        assertEquals(2, server.requestCount)
    }
}
