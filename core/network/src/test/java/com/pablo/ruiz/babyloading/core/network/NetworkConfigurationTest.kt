package com.pablo.ruiz.babyloading.core.network

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkConfigurationTest {
    @Test
    fun createNormalizesBaseUrlAndUsesDefaultTimeouts() {
        val configuration = NetworkConfiguration.create(
            baseUrl = "  https://api.example.com/v1///  ",
        )

        assertEquals("https://api.example.com/v1/", configuration.baseUrl.toString())
        assertEquals(15.seconds, configuration.connectTimeout)
        assertEquals(30.seconds, configuration.readTimeout)
        assertEquals(30.seconds, configuration.writeTimeout)
        assertEquals(60.seconds, configuration.callTimeout)
    }

    @Test
    fun createPreservesValidCustomTimeouts() {
        val configuration = NetworkConfiguration.create(
            baseUrl = "http://localhost:8080",
            connectTimeout = 250.milliseconds,
            readTimeout = 1.seconds,
            writeTimeout = 2.seconds,
            callTimeout = 3.seconds,
        )

        assertEquals("http://localhost:8080/", configuration.baseUrl.toString())
        assertEquals(250.milliseconds, configuration.connectTimeout)
        assertEquals(1.seconds, configuration.readTimeout)
        assertEquals(2.seconds, configuration.writeTimeout)
        assertEquals(3.seconds, configuration.callTimeout)
    }

    @Test
    fun createRejectsInvalidBaseUrl() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NetworkConfiguration.create(baseUrl = "not a URL")
        }

        assertEquals(
            "baseUrl must be a valid HTTP or HTTPS URL.",
            exception.message,
        )
    }

    @Test
    fun createRejectsEveryNonPositiveOrInfiniteTimeout() {
        val timeoutFactories = listOf<Pair<String, (Duration) -> NetworkConfiguration>>(
            "connectTimeout" to { timeout ->
                NetworkConfiguration.create(
                    baseUrl = VALID_BASE_URL,
                    connectTimeout = timeout,
                )
            },
            "readTimeout" to { timeout ->
                NetworkConfiguration.create(
                    baseUrl = VALID_BASE_URL,
                    readTimeout = timeout,
                )
            },
            "writeTimeout" to { timeout ->
                NetworkConfiguration.create(
                    baseUrl = VALID_BASE_URL,
                    writeTimeout = timeout,
                )
            },
            "callTimeout" to { timeout ->
                NetworkConfiguration.create(
                    baseUrl = VALID_BASE_URL,
                    callTimeout = timeout,
                )
            },
        )
        val invalidTimeouts = listOf(
            Duration.ZERO,
            (-1).milliseconds,
            1.nanoseconds,
            (Int.MAX_VALUE.toLong() + 1).milliseconds,
            Duration.INFINITE,
        )

        timeoutFactories.forEach { (timeoutName, createConfiguration) ->
            invalidTimeouts.forEach { invalidTimeout ->
                val exception = assertThrows(IllegalArgumentException::class.java) {
                    createConfiguration(invalidTimeout)
                }

                assertTrue(
                    exception.message.orEmpty().contains(timeoutName),
                )
            }
        }
    }

    private companion object {
        const val VALID_BASE_URL = "https://api.example.com/"
    }
}
