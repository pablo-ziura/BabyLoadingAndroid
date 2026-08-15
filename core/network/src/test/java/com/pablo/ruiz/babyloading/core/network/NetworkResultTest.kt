package com.pablo.ruiz.babyloading.core.network

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkResultTest {
    @Test
    fun mapTransformsSuccessValue() {
        val result = NetworkResult.Success(21)

        val mappedResult = result.map { value -> value * 2 }

        assertEquals(NetworkResult.Success(42), mappedResult)
    }

    @Test
    fun mapPreservesErrorWithoutExecutingTransform() {
        val result = NetworkResult.Error(
            NetworkError.Transport(IOException("Offline")),
        )
        var transformExecuted = false

        val mappedResult = result.map {
            transformExecuted = true
            "unreachable"
        }

        assertSame(result, mappedResult)
        assertFalse(transformExecuted)
    }
}
