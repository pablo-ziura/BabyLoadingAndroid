package com.example.babyloading.core.network

import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.serialization.SerializationException

sealed interface NetworkError {
    sealed interface Http : NetworkError {
        val statusCode: Int
        val bodyPreview: String?
    }

    data class BadRequest(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 400
    }

    data class Unauthorized(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 401
    }

    data class Forbidden(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 403
    }

    data class NotFound(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 404
    }

    data class RequestTimeout(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 408
    }

    data class Conflict(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 409
    }

    data class UnprocessableEntity(
        override val bodyPreview: String?,
    ) : Http {
        override val statusCode: Int = 422
    }

    data class RateLimited(
        override val bodyPreview: String?,
        val retryAfter: String?,
    ) : Http {
        override val statusCode: Int = 429
    }

    data class ClientError(
        override val statusCode: Int,
        override val bodyPreview: String?,
    ) : Http

    data class ServerError(
        override val statusCode: Int,
        override val bodyPreview: String?,
    ) : Http

    data class HttpError(
        override val statusCode: Int,
        override val bodyPreview: String?,
    ) : Http

    data class Transport(
        val cause: IOException,
    ) : NetworkError

    data class Timeout(
        val cause: InterruptedIOException,
    ) : NetworkError

    data class Serialization(
        val cause: SerializationException,
    ) : NetworkError

    data object EmptyBody : NetworkError

    data class Unexpected(
        val cause: Exception,
    ) : NetworkError
}
