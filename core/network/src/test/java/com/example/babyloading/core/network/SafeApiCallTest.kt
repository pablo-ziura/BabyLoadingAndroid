package com.example.babyloading.core.network

import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SafeApiCallTest {
    @Test
    fun safeApiCallReturnsBodyForEverySuccessfulHttpStatus() = runTest {
        (200..299).forEach { statusCode ->
            val result = safeApiCall {
                Response.success(statusCode, "response-$statusCode")
            }

            assertEquals(
                NetworkResult.Success("response-$statusCode"),
                result,
            )
        }
    }

    @Test
    fun safeApiCallReturnsUnitForSuccessfulEmptyBody() = runTest {
        val result = safeApiCall<Unit> {
            Response.success<Unit>(204, null)
        }

        assertEquals(NetworkResult.Success(Unit), result)
    }

    @Test
    fun safeApiCallReturnsEmptyBodyForRequiredSuccessfulBody() = runTest {
        val result = safeApiCall<String> {
            Response.success<String>(204, null)
        }

        assertEquals(
            NetworkResult.Error(NetworkError.EmptyBody),
            result,
        )
    }

    @Test
    fun safeApiCallReturnsNullForNullableSuccessfulBody() = runTest {
        val result = safeApiCall<String?> {
            Response.success<String?>(204, null)
        }

        assertEquals(NetworkResult.Success(null), result)
    }

    @Test
    fun safeApiCallMapsEverySupportedHttpError() = runTest {
        val body = """{"error":"request failed"}"""
        val expectations = listOf(
            HttpErrorExpectation(400, NetworkError.BadRequest(body)),
            HttpErrorExpectation(401, NetworkError.Unauthorized(body)),
            HttpErrorExpectation(403, NetworkError.Forbidden(body)),
            HttpErrorExpectation(404, NetworkError.NotFound(body)),
            HttpErrorExpectation(408, NetworkError.RequestTimeout(body)),
            HttpErrorExpectation(409, NetworkError.Conflict(body)),
            HttpErrorExpectation(422, NetworkError.UnprocessableEntity(body)),
            HttpErrorExpectation(
                statusCode = 429,
                expectedError = NetworkError.RateLimited(
                    bodyPreview = body,
                    retryAfter = "120",
                ),
                headers = headersOf("Retry-After", "120"),
            ),
            HttpErrorExpectation(
                statusCode = 418,
                expectedError = NetworkError.ClientError(
                    statusCode = 418,
                    bodyPreview = body,
                ),
            ),
            HttpErrorExpectation(
                statusCode = 503,
                expectedError = NetworkError.ServerError(
                    statusCode = 503,
                    bodyPreview = body,
                ),
            ),
            HttpErrorExpectation(
                statusCode = 300,
                expectedError = NetworkError.HttpError(
                    statusCode = 300,
                    bodyPreview = body,
                ),
            ),
        )

        expectations.forEach { expectation ->
            val result = safeApiCall<String> {
                errorResponse(
                    statusCode = expectation.statusCode,
                    body = body,
                    headers = expectation.headers,
                )
            }

            assertEquals(
                expectation.expectedError,
                result.requireNetworkError(),
            )
        }
    }

    @Test
    fun safeApiCallLimitsErrorBodyPreviewToEightKibibytes() = runTest {
        val oversizedBody = "x".repeat(ERROR_PREVIEW_LIMIT_BYTES + 512)

        val result = safeApiCall<String> {
            errorResponse(
                statusCode = 400,
                body = oversizedBody,
            )
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.BadRequest)
        assertEquals(
            "x".repeat(ERROR_PREVIEW_LIMIT_BYTES),
            (error as NetworkError.BadRequest).bodyPreview,
        )
    }

    @Test
    fun safeApiCallMapsHttpExceptionResponse() = runTest {
        val response = errorResponse<String>(
            statusCode = 404,
            body = """{"error":"missing"}""",
        )

        val result = safeApiCall<String> {
            throw HttpException(response)
        }

        assertEquals(
            NetworkError.NotFound("""{"error":"missing"}"""),
            result.requireNetworkError(),
        )
    }

    @Test
    fun safeApiCallMapsSocketTimeoutExceptionAndPreservesCause() = runTest {
        val exception = SocketTimeoutException("Timed out")

        val result = safeApiCall<String> {
            throw exception
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.Timeout)
        assertSame(exception, (error as NetworkError.Timeout).cause)
    }

    @Test
    fun safeApiCallMapsCallTimeoutAndPreservesCause() = runTest {
        val exception = InterruptedIOException("timeout")

        val result = safeApiCall<String> {
            throw exception
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.Timeout)
        assertSame(exception, (error as NetworkError.Timeout).cause)
    }

    @Test
    fun safeApiCallMapsIOExceptionAndPreservesCause() = runTest {
        val exception = IOException("Connection reset")

        val result = safeApiCall<String> {
            throw exception
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.Transport)
        assertSame(exception, (error as NetworkError.Transport).cause)
    }

    @Test
    fun safeApiCallMapsSerializationExceptionAndPreservesCause() = runTest {
        val exception = SerializationException("Malformed payload")

        val result = safeApiCall<String> {
            throw exception
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.Serialization)
        assertSame(exception, (error as NetworkError.Serialization).cause)
    }

    @Test
    fun safeApiCallMapsUnexpectedExceptionAndPreservesCause() = runTest {
        val exception = IllegalStateException("Unexpected state")

        val result = safeApiCall<String> {
            throw exception
        }

        val error = result.requireNetworkError()
        assertTrue(error is NetworkError.Unexpected)
        assertSame(exception, (error as NetworkError.Unexpected).cause)
    }

    @Test
    fun safeApiCallRethrowsCancellationException() = runTest {
        val exception = CancellationException("Cancelled")

        try {
            safeApiCall<String> {
                throw exception
            }
            fail("CancellationException was not rethrown")
        } catch (caught: CancellationException) {
            assertSame(exception, caught)
        }
    }

    private fun NetworkResult<*>.requireNetworkError(): NetworkError {
        assertTrue(this is NetworkResult.Error)
        return (this as NetworkResult.Error).networkError
    }

    private fun <T> errorResponse(
        statusCode: Int,
        body: String,
        headers: Headers = Headers.Builder().build(),
    ): Response<T> {
        val rawResponse = okhttp3.Response.Builder()
            .request(
                Request.Builder()
                    .url("https://example.invalid/")
                    .build(),
            )
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message("HTTP $statusCode")
            .headers(headers)
            .build()

        return Response.error(
            body.toResponseBody(JSON_MEDIA_TYPE),
            rawResponse,
        )
    }

    private data class HttpErrorExpectation(
        val statusCode: Int,
        val expectedError: NetworkError,
        val headers: Headers = Headers.Builder().build(),
    )

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val ERROR_PREVIEW_LIMIT_BYTES = 8 * 1024
    }
}
