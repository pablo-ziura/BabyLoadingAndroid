package com.example.babyloading.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class DefaultHeadersInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestWithDefaultHeaders = if (request.header(ACCEPT_HEADER) == null) {
            request.newBuilder()
                .header(ACCEPT_HEADER, JSON_MEDIA_TYPE)
                .build()
        } else {
            request
        }

        return chain.proceed(requestWithDefaultHeaders)
    }

    private companion object {
        const val ACCEPT_HEADER = "Accept"
        const val JSON_MEDIA_TYPE = "application/json"
    }
}
