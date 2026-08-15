package com.pablo.ruiz.babyloading.core.network

import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

suspend inline fun <reified T> safeApiCall(
    apiCall: suspend () -> Response<T>,
): NetworkResult<T> = try {
    val response = apiCall()
    if (response.isSuccessful) {
        val body = response.body()
        when {
            body != null -> NetworkResult.Success(body)
            T::class == Unit::class -> {
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(Unit as T)
            }
            null is T -> {
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(null as T)
            }
            else -> NetworkResult.Error(NetworkError.EmptyBody)
        }
    } else {
        NetworkResult.Error(response.toNetworkError())
    }
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (httpException: HttpException) {
    val networkError = httpException.response()?.toNetworkError()
        ?: NetworkError.HttpError(
            statusCode = httpException.code(),
            bodyPreview = null,
        )
    NetworkResult.Error(networkError)
} catch (interruptedIOException: InterruptedIOException) {
    NetworkResult.Error(NetworkError.Timeout(interruptedIOException))
} catch (ioException: IOException) {
    NetworkResult.Error(NetworkError.Transport(ioException))
} catch (serializationException: SerializationException) {
    NetworkResult.Error(NetworkError.Serialization(serializationException))
} catch (exception: Exception) {
    NetworkResult.Error(NetworkError.Unexpected(exception))
}

@PublishedApi
internal fun Response<*>.toNetworkError(): NetworkError {
    val statusCode = code()
    val bodyPreview = errorBody().readPreview()

    return when (statusCode) {
        400 -> NetworkError.BadRequest(bodyPreview)
        401 -> NetworkError.Unauthorized(bodyPreview)
        403 -> NetworkError.Forbidden(bodyPreview)
        404 -> NetworkError.NotFound(bodyPreview)
        408 -> NetworkError.RequestTimeout(bodyPreview)
        409 -> NetworkError.Conflict(bodyPreview)
        422 -> NetworkError.UnprocessableEntity(bodyPreview)
        429 -> NetworkError.RateLimited(
            bodyPreview = bodyPreview,
            retryAfter = headers()[RETRY_AFTER_HEADER],
        )
        in 400..499 -> NetworkError.ClientError(
            statusCode = statusCode,
            bodyPreview = bodyPreview,
        )
        in 500..599 -> NetworkError.ServerError(
            statusCode = statusCode,
            bodyPreview = bodyPreview,
        )
        else -> NetworkError.HttpError(
            statusCode = statusCode,
            bodyPreview = bodyPreview,
        )
    }
}

private fun ResponseBody?.readPreview(): String? {
    if (this == null) return null

    return try {
        val charset = contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        byteStream().use { inputStream ->
            inputStream.readNBytes(MAX_ERROR_BODY_PREVIEW_BYTES)
                .toString(charset)
                .takeIf(String::isNotEmpty)
        }
    } catch (_: IOException) {
        null
    }
}

private const val MAX_ERROR_BODY_PREVIEW_BYTES = 8 * 1024
private const val RETRY_AFTER_HEADER = "Retry-After"
