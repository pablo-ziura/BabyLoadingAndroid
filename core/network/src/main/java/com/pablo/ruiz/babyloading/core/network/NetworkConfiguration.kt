package com.pablo.ruiz.babyloading.core.network

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class NetworkConfiguration private constructor(
    internal val baseUrl: HttpUrl,
    val connectTimeout: Duration,
    val readTimeout: Duration,
    val writeTimeout: Duration,
    val callTimeout: Duration,
) {
    companion object {
        fun create(
            baseUrl: String,
            connectTimeout: Duration = 15.seconds,
            readTimeout: Duration = 30.seconds,
            writeTimeout: Duration = 30.seconds,
            callTimeout: Duration = 60.seconds,
        ): NetworkConfiguration {
            validateTimeout("connectTimeout", connectTimeout)
            validateTimeout("readTimeout", readTimeout)
            validateTimeout("writeTimeout", writeTimeout)
            validateTimeout("callTimeout", callTimeout)

            val parsedBaseUrl = requireNotNull(baseUrl.trim().toHttpUrlOrNull()) {
                "baseUrl must be a valid HTTP or HTTPS URL."
            }
            require(parsedBaseUrl.scheme == HTTP_SCHEME || parsedBaseUrl.scheme == HTTPS_SCHEME) {
                "baseUrl must use HTTP or HTTPS."
            }

            val normalizedPath = parsedBaseUrl.encodedPath.trimEnd('/') + "/"
            val normalizedBaseUrl = parsedBaseUrl.newBuilder()
                .encodedPath(normalizedPath)
                .build()

            return NetworkConfiguration(
                baseUrl = normalizedBaseUrl,
                connectTimeout = connectTimeout,
                readTimeout = readTimeout,
                writeTimeout = writeTimeout,
                callTimeout = callTimeout,
            )
        }

        private fun validateTimeout(name: String, timeout: Duration) {
            require(
                timeout.isFinite() &&
                    timeout.inWholeMilliseconds in 1..Int.MAX_VALUE.toLong(),
            ) {
                "$name must be between 1 and ${Int.MAX_VALUE} milliseconds."
            }
        }

        private const val HTTP_SCHEME = "http"
        private const val HTTPS_SCHEME = "https"
    }
}
