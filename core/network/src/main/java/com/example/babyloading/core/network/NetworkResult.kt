package com.example.babyloading.core.network

sealed interface NetworkResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : NetworkResult<T>

    data class Error(
        val networkError: NetworkError,
    ) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(
    transform: (T) -> R,
): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(value))
    is NetworkResult.Error -> this
}
