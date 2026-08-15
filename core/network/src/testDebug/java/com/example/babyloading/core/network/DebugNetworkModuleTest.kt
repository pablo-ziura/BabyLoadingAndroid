package com.example.babyloading.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugNetworkModuleTest {
    @Test
    fun `provides basic logging with sensitive headers redacted`() {
        val logMessages = mutableListOf<String>()
        val loggingInterceptor = createHttpLoggingInterceptor(logMessages::add)
        assertEquals(HttpLoggingInterceptor.Level.BASIC, loggingInterceptor.level)
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        val server = MockWebServer()
        server.start()

        try {
            server.enqueue(
                MockResponse.Builder()
                    .addHeader("Set-Cookie", "session=response-secret")
                    .build(),
            )
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            val request = Request.Builder()
                .url(server.url("/logging"))
                .header(AUTHORIZATION_HEADER, "Bearer request-secret")
                .header("Cookie", "session=request-secret")
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
            }
        } finally {
            server.close()
        }

        assertTrue(logMessages.any { it == "Authorization: ██" })
        assertTrue(logMessages.any { it == "Cookie: ██" })
        assertTrue(logMessages.any { it == "Set-Cookie: ██" })
        assertFalse(logMessages.any { "secret" in it })
    }
}
