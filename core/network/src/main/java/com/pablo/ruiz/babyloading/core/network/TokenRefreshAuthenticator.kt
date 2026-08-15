package com.pablo.ruiz.babyloading.core.network

import java.util.Optional
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val accessTokenStore: Optional<AccessTokenStore>,
    private val accessTokenRefresher: Optional<AccessTokenRefresher>,
) : Authenticator {
    private val refreshLock = Any()
    private var activeRefreshFlight: RefreshFlight? = null

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.authenticationAttemptCount() > MAX_AUTHENTICATION_ATTEMPTS) {
            return null
        }

        val tokenStore = accessTokenStore.orElse(null) ?: return null
        val tokenRefresher = accessTokenRefresher.orElse(null) ?: return null
        val authenticationTag = response.request.tag(
            BearerTokenAuthenticationTag::class.java,
        ) ?: return null
        val authorizationHeader = response.request.header(AUTHORIZATION_HEADER)
        val failedAccessToken = authorizationHeader.bearerToken()

        if (failedAccessToken == null || failedAccessToken != authenticationTag.accessToken) {
            return null
        }

        var leadsRefresh = false
        val refreshFlight = synchronized(refreshLock) {
            val currentAccessToken = try {
                tokenStore.getAccessToken().normalizedToken()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return null
            } catch (_: Exception) {
                return null
            }

            if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
                return response.request.withBearerToken(currentAccessToken)
            }

            activeRefreshFlight ?: RefreshFlight().also { newRefreshFlight ->
                activeRefreshFlight = newRefreshFlight
                leadsRefresh = true
            }
        }

        val refreshOutcome = if (leadsRefresh) {
            executeRefresh(
                refreshFlight = refreshFlight,
                tokenStore = tokenStore,
                tokenRefresher = tokenRefresher,
                failedAccessToken = failedAccessToken,
            )
        } else {
            refreshFlight.awaitOutcome()
        }

        return when (refreshOutcome) {
            is RefreshOutcome.Success -> refreshOutcome.accessToken
                .takeIf { it != failedAccessToken }
                ?.let { accessToken -> response.request.withBearerToken(accessToken) }

            RefreshOutcome.Failure -> null
        }
    }

    private fun executeRefresh(
        refreshFlight: RefreshFlight,
        tokenStore: AccessTokenStore,
        tokenRefresher: AccessTokenRefresher,
        failedAccessToken: String?,
    ): RefreshOutcome {
        var refreshOutcome: RefreshOutcome = RefreshOutcome.Failure

        try {
            val refreshedAccessToken = try {
                tokenRefresher.refreshAccessToken(failedAccessToken).normalizedToken()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                null
            } catch (_: Exception) {
                null
            }

            if (refreshedAccessToken == null || refreshedAccessToken == failedAccessToken) {
                return refreshOutcome
            }

            try {
                tokenStore.updateAccessToken(refreshedAccessToken)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return refreshOutcome
            } catch (_: Exception) {
                return refreshOutcome
            }

            refreshOutcome = RefreshOutcome.Success(refreshedAccessToken)
            return refreshOutcome
        } finally {
            synchronized(refreshLock) {
                if (activeRefreshFlight === refreshFlight) {
                    activeRefreshFlight = null
                }
                refreshFlight.complete(refreshOutcome)
            }
        }
    }

    private fun Response.authenticationAttemptCount(): Int {
        var authenticationAttemptCount = 0
        var currentResponse: Response? = this

        while (currentResponse != null) {
            if (currentResponse.code == HTTP_UNAUTHORIZED) {
                authenticationAttemptCount += 1
            }
            currentResponse = currentResponse.priorResponse
        }

        return authenticationAttemptCount
    }

    private fun String?.bearerToken(): String? {
        val value = this ?: return null
        if (!value.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            return null
        }

        return value.substring(BEARER_PREFIX.length).normalizedToken()
    }

    private fun Request.withBearerToken(accessToken: String): Request = newBuilder()
        .header(AUTHORIZATION_HEADER, "$BEARER_SCHEME $accessToken")
        .tag(
            BearerTokenAuthenticationTag::class.java,
            BearerTokenAuthenticationTag(accessToken),
        )
        .build()

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val MAX_AUTHENTICATION_ATTEMPTS = 1
        const val BEARER_PREFIX = "$BEARER_SCHEME "
    }

    private class RefreshFlight {
        private val completion = CountDownLatch(1)

        @Volatile
        private var outcome: RefreshOutcome = RefreshOutcome.Failure

        fun complete(outcome: RefreshOutcome) {
            this.outcome = outcome
            completion.countDown()
        }

        fun awaitOutcome(): RefreshOutcome {
            try {
                completion.await()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return RefreshOutcome.Failure
            }

            return outcome
        }
    }

    private sealed interface RefreshOutcome {
        data class Success(
            val accessToken: String,
        ) : RefreshOutcome

        data object Failure : RefreshOutcome
    }
}
