package com.pablo.ruiz.babyloading.core.network

import java.util.Optional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenRefreshAuthenticatorTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `refreshes token updates store and retries request once`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val refresher = AccessTokenRefreshDataSource { expiredAccessToken ->
            assertEquals("expired-token", expiredAccessToken)
            refreshCalls.incrementAndGet()
            "fresh-token"
        }
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 200))

        executeRequest(createAuthenticatedClient(tokenDataSource, refresher)).use { response ->
            assertEquals(200, response.code)
        }

        assertEquals("Bearer expired-token", server.takeRequest().headers[AUTHORIZATION_HEADER])
        assertEquals("Bearer fresh-token", server.takeRequest().headers[AUTHORIZATION_HEADER])
        assertEquals("fresh-token", tokenDataSource.currentToken())
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `does not retry when token refresh fails`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val refresher = AccessTokenRefreshDataSource {
            refreshCalls.incrementAndGet()
            null
        }
        server.enqueue(MockResponse(code = 401))

        executeRequest(createAuthenticatedClient(tokenDataSource, refresher)).use { response ->
            assertEquals(401, response.code)
        }

        assertEquals(1, server.requestCount)
        assertEquals("expired-token", tokenDataSource.currentToken())
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `does not retry when token refresher throws`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val refresher = AccessTokenRefreshDataSource {
            refreshCalls.incrementAndGet()
            throw IllegalStateException("Refresh failed")
        }
        server.enqueue(MockResponse(code = 401))

        executeRequest(createAuthenticatedClient(tokenDataSource, refresher)).use { response ->
            assertEquals(401, response.code)
        }

        assertEquals(1, server.requestCount)
        assertEquals("expired-token", tokenDataSource.currentToken())
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `allows a later refresh after a failed flight completes`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val authenticator = TokenRefreshAuthenticator(
            accessTokenDataSource = Optional.of(tokenDataSource),
            accessTokenRefreshDataSource = Optional.of(
                AccessTokenRefreshDataSource {
                    if (refreshCalls.incrementAndGet() == 1) null else "fresh-token"
                },
            ),
        )

        val firstRetryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse("expired-token"),
        )
        val secondRetryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse("expired-token"),
        )

        assertNull(firstRetryRequest)
        assertEquals("Bearer fresh-token", secondRetryRequest?.header(AUTHORIZATION_HEADER))
        assertEquals("fresh-token", tokenDataSource.currentToken())
        assertEquals(2, refreshCalls.get())
    }

    @Test
    fun `does not retry when refresher returns same token`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val refresher = AccessTokenRefreshDataSource {
            refreshCalls.incrementAndGet()
            "  expired-token  "
        }
        server.enqueue(MockResponse(code = 401))

        executeRequest(createAuthenticatedClient(tokenDataSource, refresher)).use { response ->
            assertEquals(401, response.code)
        }

        assertEquals(1, server.requestCount)
        assertEquals("expired-token", tokenDataSource.currentToken())
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `stops after one authentication retry`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("expired-token")
        val refreshCalls = AtomicInteger()
        val refresher = AccessTokenRefreshDataSource {
            refreshCalls.incrementAndGet()
            "fresh-token"
        }
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 401))

        executeRequest(createAuthenticatedClient(tokenDataSource, refresher)).use { response ->
            assertEquals(401, response.code)
        }

        assertEquals(2, server.requestCount)
        assertEquals("Bearer expired-token", server.takeRequest().headers[AUTHORIZATION_HEADER])
        assertEquals("Bearer fresh-token", server.takeRequest().headers[AUTHORIZATION_HEADER])
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `retries with token already refreshed by another request`() {
        val tokenDataSource = RefreshTestAccessTokenDataSource("fresh-token")
        val refreshCalls = AtomicInteger()
        val authenticator = TokenRefreshAuthenticator(
            accessTokenDataSource = Optional.of(tokenDataSource),
            accessTokenRefreshDataSource = Optional.of(
                AccessTokenRefreshDataSource {
                    refreshCalls.incrementAndGet()
                    "unused-token"
                },
            ),
        )

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse("expired-token"),
        )

        assertEquals("Bearer fresh-token", retryRequest?.header(AUTHORIZATION_HEADER))
        assertEquals(0, refreshCalls.get())
        assertEquals("fresh-token", tokenDataSource.currentToken())
    }

    @Test
    fun `coalesces concurrent refresh attempts into a single refresh`() {
        val result = executeConcurrentAuthentication {
            "fresh-token"
        }

        result.retryRequests.forEach { retryRequest ->
            assertEquals(
                "Bearer fresh-token",
                retryRequest?.header(AUTHORIZATION_HEADER),
            )
        }
        assertEquals(1, result.refreshCallCount)
        assertEquals("fresh-token", result.storedToken)
    }

    @Test
    fun `coalesces concurrent failed refresh attempts`() {
        val result = executeConcurrentAuthentication {
            null
        }

        assertTrue(result.retryRequests.all { it == null })
        assertEquals(1, result.refreshCallCount)
        assertEquals("expired-token", result.storedToken)
    }

    @Test
    fun `coalesces concurrent refresh attempts returning the same token`() {
        val result = executeConcurrentAuthentication {
            "expired-token"
        }

        assertTrue(result.retryRequests.all { it == null })
        assertEquals(1, result.refreshCallCount)
        assertEquals("expired-token", result.storedToken)
    }

    @Test
    fun `coalesces concurrent refresh attempts that throw`() {
        val result = executeConcurrentAuthentication {
            throw IllegalStateException("Refresh failed")
        }

        assertTrue(result.retryRequests.all { it == null })
        assertEquals(1, result.refreshCallCount)
        assertEquals("expired-token", result.storedToken)
    }

    @Test
    fun `returns null when token store is absent`() {
        val authenticator = TokenRefreshAuthenticator(
            accessTokenDataSource = Optional.empty(),
            accessTokenRefreshDataSource = Optional.of(AccessTokenRefreshDataSource { "fresh-token" }),
        )

        assertNull(
            authenticator.authenticate(
                route = null,
                response = unauthorizedResponse("expired-token"),
            ),
        )
    }

    @Test
    fun `does not refresh a caller provided bearer token`() {
        val refreshCalls = AtomicInteger()
        val authenticator = TokenRefreshAuthenticator(
            accessTokenDataSource = Optional.of(RefreshTestAccessTokenDataSource("stored-token")),
            accessTokenRefreshDataSource = Optional.of(
                AccessTokenRefreshDataSource {
                    refreshCalls.incrementAndGet()
                    "fresh-token"
                },
            ),
        )

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(
                accessToken = "caller-token",
                isManagedByNetworkModule = false,
            ),
        )

        assertNull(retryRequest)
        assertEquals(0, refreshCalls.get())
    }

    private fun createAuthenticatedClient(
        tokenDataSource: AccessTokenDataSource,
        tokenRefreshDataSource: AccessTokenRefreshDataSource,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            AuthInterceptor(
                accessTokenDataSource = Optional.of(tokenDataSource),
                networkConfiguration = NetworkConfiguration.create(server.url("/").toString()),
            ),
        )
        .authenticator(
            TokenRefreshAuthenticator(
                accessTokenDataSource = Optional.of(tokenDataSource),
                accessTokenRefreshDataSource = Optional.of(tokenRefreshDataSource),
            ),
        )
        .build()

    private fun executeRequest(client: OkHttpClient): Response = client.newCall(
        Request.Builder()
            .url(server.url("/refresh"))
            .build(),
    ).execute()

    private fun unauthorizedResponse(
        accessToken: String,
        isManagedByNetworkModule: Boolean = true,
    ): Response {
        val requestBuilder = Request.Builder()
            .url("https://example.invalid/resource")
            .header(AUTHORIZATION_HEADER, "Bearer $accessToken")

        if (isManagedByNetworkModule) {
            requestBuilder.tag(
                BearerTokenAuthenticationTag::class.java,
                BearerTokenAuthenticationTag(accessToken),
            )
        }

        return Response.Builder()
            .request(requestBuilder.build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }

    private fun executeConcurrentAuthentication(
        refresh: () -> String?,
    ): ConcurrentAuthenticationResult {
        val requestCount = 8
        val refreshCalls = AtomicInteger()
        val refreshStarted = CountDownLatch(1)
        val releaseRefresh = CountDownLatch(1)
        val tokenReadsObserved = CountDownLatch(requestCount)
        val workersReady = CountDownLatch(requestCount)
        val startWorkers = CountDownLatch(1)
        val authenticationInvoked = CountDownLatch(requestCount)
        val tokenDataSource = RefreshTestAccessTokenDataSource(
            token = "expired-token",
            tokenReadsObserved = tokenReadsObserved,
        )
        val authenticator = TokenRefreshAuthenticator(
            accessTokenDataSource = Optional.of(tokenDataSource),
            accessTokenRefreshDataSource = Optional.of(
                AccessTokenRefreshDataSource {
                    refreshCalls.incrementAndGet()
                    refreshStarted.countDown()
                    assertTrue(releaseRefresh.await(5, TimeUnit.SECONDS))
                    refresh()
                },
            ),
        )
        val executor = Executors.newFixedThreadPool(requestCount)

        return try {
            val futures = List(requestCount) {
                executor.submit<Request?> {
                    workersReady.countDown()
                    assertTrue(startWorkers.await(5, TimeUnit.SECONDS))
                    authenticationInvoked.countDown()
                    authenticator.authenticate(
                        route = null,
                        response = unauthorizedResponse("expired-token"),
                    )
                }
            }

            assertTrue(workersReady.await(5, TimeUnit.SECONDS))
            startWorkers.countDown()
            assertTrue(authenticationInvoked.await(5, TimeUnit.SECONDS))
            assertTrue(refreshStarted.await(5, TimeUnit.SECONDS))
            assertTrue(tokenReadsObserved.await(5, TimeUnit.SECONDS))
            releaseRefresh.countDown()

            ConcurrentAuthenticationResult(
                retryRequests = futures.map { future ->
                    future.get(5, TimeUnit.SECONDS)
                },
                refreshCallCount = refreshCalls.get(),
                storedToken = tokenDataSource.currentToken(),
            )
        } finally {
            releaseRefresh.countDown()
            executor.shutdownNow()
        }
    }

    private data class ConcurrentAuthenticationResult(
        val retryRequests: List<Request?>,
        val refreshCallCount: Int,
        val storedToken: String?,
    )

    private class RefreshTestAccessTokenDataSource(
        @Volatile private var token: String?,
        private val tokenReadsObserved: CountDownLatch? = null,
    ) : AccessTokenDataSource {
        override fun getAccessToken(): String? {
            tokenReadsObserved?.countDown()
            return token
        }

        override fun updateAccessToken(accessToken: String?) {
            token = accessToken
        }

        fun currentToken(): String? = token
    }
}
