package com.pablo.ruiz.babyloading.core.network

import java.util.Optional
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkModuleTest {
    @Test
    fun `provides Json configured to ignore unknown keys`() {
        val json = NetworkModule.provideJson()
        val payload = json.decodeFromJsonElement<JsonPayload>(
            json.parseToJsonElement(
                """{"knownValue":"expected","unknownValue":"ignored"}""",
            ),
        )

        assertEquals(JsonPayload("expected"), payload)
    }

    @Test
    fun `provides unauthenticated client with configured timeouts and interceptors`() {
        val configuration = NetworkConfiguration.create(
            baseUrl = "https://example.invalid/api",
            connectTimeout = 1_001.milliseconds,
            readTimeout = 2_002.milliseconds,
            writeTimeout = 3_003.milliseconds,
            callTimeout = 4_004.milliseconds,
        )

        val client = NetworkModule.provideUnauthenticatedClient(
            networkConfiguration = configuration,
            defaultHeadersInterceptor = DefaultHeadersInterceptor(),
            networkInterceptors = emptySet(),
        )

        assertEquals(1_001, client.connectTimeoutMillis)
        assertEquals(2_002, client.readTimeoutMillis)
        assertEquals(3_003, client.writeTimeoutMillis)
        assertEquals(4_004, client.callTimeoutMillis)

        assertEquals(1, client.interceptors.size)
    }

    @Test
    fun `provides Retrofit instances with normalized base URL and expected clients`() {
        val configuration = NetworkConfiguration.create(
            baseUrl = "https://example.invalid/api",
        )
        val json = NetworkModule.provideJson()
        val unauthenticatedClient = NetworkModule.provideUnauthenticatedClient(
            networkConfiguration = configuration,
            defaultHeadersInterceptor = DefaultHeadersInterceptor(),
            networkInterceptors = emptySet(),
        )
        val authenticatedClient = NetworkModule.provideAuthenticatedClient(
            unauthenticatedClient = unauthenticatedClient,
            authInterceptor = AuthInterceptor(
                accessTokenDataSource = Optional.empty(),
                networkConfiguration = configuration,
            ),
            tokenRefreshAuthenticator = TokenRefreshAuthenticator(
                accessTokenDataSource = Optional.empty(),
                accessTokenRefreshDataSource = Optional.empty(),
            ),
        )

        val unauthenticatedRetrofit = NetworkModule.provideUnauthenticatedRetrofit(
            networkConfiguration = configuration,
            json = json,
            unauthenticatedClient = unauthenticatedClient,
        )
        val authenticatedRetrofit = NetworkModule.provideAuthenticatedRetrofit(
            networkConfiguration = configuration,
            json = json,
            authenticatedClient = authenticatedClient,
        )

        assertEquals("https://example.invalid/api/", unauthenticatedRetrofit.baseUrl().toString())
        assertEquals("https://example.invalid/api/", authenticatedRetrofit.baseUrl().toString())
        assertSame(unauthenticatedClient, unauthenticatedRetrofit.callFactory())
        assertSame(authenticatedClient, authenticatedRetrofit.callFactory())
    }

    @Test
    fun `authenticated client adds bearer while unauthenticated client omits it`() {
        val server = MockWebServer()
        server.start()

        try {
            val configuration = NetworkConfiguration.create(server.url("/").toString())
            val tokenDataSource = object : AccessTokenDataSource {
                override fun getAccessToken(): String = "access-token"

                override fun updateAccessToken(accessToken: String?) = Unit
            }
            val unauthenticatedClient = NetworkModule.provideUnauthenticatedClient(
                networkConfiguration = configuration,
                defaultHeadersInterceptor = DefaultHeadersInterceptor(),
                networkInterceptors = emptySet(),
            )
            val authenticatedClient = NetworkModule.provideAuthenticatedClient(
                unauthenticatedClient = unauthenticatedClient,
                authInterceptor = AuthInterceptor(
                    accessTokenDataSource = Optional.of(tokenDataSource),
                    networkConfiguration = configuration,
                ),
                tokenRefreshAuthenticator = TokenRefreshAuthenticator(
                    accessTokenDataSource = Optional.of(tokenDataSource),
                    accessTokenRefreshDataSource = Optional.empty(),
                ),
            )
            repeat(2) {
                server.enqueue(MockResponse())
            }

            unauthenticatedClient.executeRequest(server)
            authenticatedClient.executeRequest(server)

            assertNull(server.takeRequest().headers[AUTHORIZATION_HEADER])
            assertEquals(
                "Bearer access-token",
                server.takeRequest().headers[AUTHORIZATION_HEADER],
            )
        } finally {
            server.close()
        }
    }

    private fun OkHttpClient.executeRequest(server: MockWebServer) {
        newCall(
            Request.Builder()
                .url(server.url("/client"))
                .build(),
        ).execute().use { response ->
            assertEquals(200, response.code)
        }
    }

    @Serializable
    private data class JsonPayload(
        val knownValue: String,
    )
}
