package com.pablo.ruiz.babyloading.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {
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
    fun `omits authorization header when token store is absent`() {
        val interceptor = createInterceptor()

        assertNull(executeRequest(interceptor).headers[AUTHORIZATION_HEADER])
    }

    @Test
    fun `omits authorization header when access token is blank`() {
        val tokenDataSource = AuthTestAccessTokenDataSource(" \t ")
        val interceptor = createInterceptor(tokenDataSource)

        assertNull(executeRequest(interceptor).headers[AUTHORIZATION_HEADER])
    }

    @Test
    fun `adds trimmed bearer access token`() {
        val tokenDataSource = AuthTestAccessTokenDataSource("  access-token  ")
        val interceptor = createInterceptor(tokenDataSource)

        assertEquals(
            "Bearer access-token",
            executeRequest(interceptor).headers[AUTHORIZATION_HEADER],
        )
    }

    @Test
    fun `preserves explicit authorization header without reading token store`() {
        val tokenDataSource = object : AccessTokenDataSource {
            override fun getAccessToken(): String? = error("Token store must not be read")

            override fun updateAccessToken(accessToken: String?) = Unit
        }
        val interceptor = createInterceptor(tokenDataSource)

        val recordedRequest = executeRequest(
            interceptor = interceptor,
            authorizationHeader = "Basic explicit-credentials",
        )

        assertEquals(
            "Basic explicit-credentials",
            recordedRequest.headers[AUTHORIZATION_HEADER],
        )
    }

    @Test
    fun `omits authorization header for a different origin`() {
        val interceptor = createInterceptor(
            tokenDataSource = AuthTestAccessTokenDataSource("access-token"),
            baseUrl = "https://api.example.invalid/",
        )

        assertNull(executeRequest(interceptor).headers[AUTHORIZATION_HEADER])
    }

    private fun createInterceptor(
        tokenDataSource: AccessTokenDataSource? = null,
        baseUrl: String = server.url("/").toString(),
    ): AuthInterceptor = AuthInterceptor(
        accessTokenDataSource = java.util.Optional.ofNullable(tokenDataSource),
        networkConfiguration = NetworkConfiguration.create(baseUrl),
    )

    private fun executeRequest(
        interceptor: AuthInterceptor,
        authorizationHeader: String? = null,
    ) = server.enqueue(MockResponse()).let {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        val requestBuilder = Request.Builder()
            .url(server.url("/auth"))

        authorizationHeader?.let {
            requestBuilder.header(AUTHORIZATION_HEADER, it)
        }

        client.newCall(requestBuilder.build())
            .execute()
            .use { response -> assertEquals(200, response.code) }

        server.takeRequest()
    }

    private class AuthTestAccessTokenDataSource(
        @Volatile private var token: String?,
    ) : AccessTokenDataSource {
        override fun getAccessToken(): String? = token

        override fun updateAccessToken(accessToken: String?) {
            token = accessToken
        }
    }
}
