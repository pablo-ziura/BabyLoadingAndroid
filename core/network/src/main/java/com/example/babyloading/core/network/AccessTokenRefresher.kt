package com.example.babyloading.core.network

/** Implementations execute synchronously and must use the unauthenticated client. */
fun interface AccessTokenRefresher {
    fun refreshAccessToken(expiredAccessToken: String?): String?
}
