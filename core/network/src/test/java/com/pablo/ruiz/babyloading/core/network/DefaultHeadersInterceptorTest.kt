package com.pablo.ruiz.babyloading.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DefaultHeadersInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(DefaultHeadersInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `adds application json accept header when request omits it`() {
        val recordedRequest = executeRequest()

        assertEquals("application/json", recordedRequest.headers["Accept"])
    }

    @Test
    fun `preserves explicit accept header`() {
        val recordedRequest = executeRequest(acceptHeader = "application/problem+json")

        assertEquals("application/problem+json", recordedRequest.headers["Accept"])
    }

    private fun executeRequest(acceptHeader: String? = null) =
        server.enqueue(MockResponse()).let {
            val requestBuilder = Request.Builder()
                .url(server.url("/headers"))

            acceptHeader?.let { requestBuilder.header("Accept", it) }

            client.newCall(requestBuilder.build())
                .execute()
                .use { response -> assertEquals(200, response.code) }

            server.takeRequest()
        }
}
