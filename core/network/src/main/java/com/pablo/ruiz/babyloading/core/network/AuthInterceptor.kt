package com.pablo.ruiz.babyloading.core.network

import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class AuthInterceptor @Inject constructor(
    private val accessTokenStore: Optional<AccessTokenStore>,
    private val networkConfiguration: NetworkConfiguration,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (
            request.header(AUTHORIZATION_HEADER) != null ||
            !request.url.hasSameOriginAs(networkConfiguration.baseUrl)
        ) {
            return chain.proceed(request)
        }

        val accessToken = accessTokenStore
            .orElse(null)
            ?.getAccessToken()
            .normalizedToken()

        val authenticatedRequest = if (accessToken == null) {
            request
        } else {
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_SCHEME $accessToken")
                .tag(
                    BearerTokenAuthenticationTag::class.java,
                    BearerTokenAuthenticationTag(accessToken),
                )
                .build()
        }

        return chain.proceed(authenticatedRequest)
    }
}

internal data class BearerTokenAuthenticationTag(
    val accessToken: String,
)

private fun HttpUrl.hasSameOriginAs(other: HttpUrl): Boolean =
    scheme == other.scheme &&
        host == other.host &&
        port == other.port

internal const val AUTHORIZATION_HEADER = "Authorization"
internal const val BEARER_SCHEME = "Bearer"

internal fun String?.normalizedToken(): String? = this
    ?.trim()
    ?.takeIf(String::isNotEmpty)
