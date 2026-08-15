package com.example.babyloading.core.network

/** Implementations must make reads and updates thread-safe. */
interface AccessTokenStore {
    fun getAccessToken(): String?

    fun updateAccessToken(accessToken: String?)
}
