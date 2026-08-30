package com.pablo.ruiz.babyloading.core.network

/** Implementations must make reads and updates thread-safe. */
interface AccessTokenDataSource {
    fun getAccessToken(): String?

    fun updateAccessToken(accessToken: String?)
}
