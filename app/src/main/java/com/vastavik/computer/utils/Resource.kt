package com.vastavik.computer.utils

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null, val exception: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
        is Loading -> throw IllegalStateException("Resource is still loading")
    }

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, code, exception)
        is Loading -> Loading
    }

    fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    fun onError(action: (String, Int?, Throwable?) -> Unit): Resource<T> {
        if (this is Error) action(message, code, exception)
        return this
    }

    fun onLoading(action: () -> Unit): Resource<T> {
        if (this is Loading) action()
        return this
    }

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(message: String, code: Int? = null, exception: Throwable? = null): Resource<Nothing> =
            Error(message, code, exception)
        fun loading(): Resource<Nothing> = Loading

        fun <T> fromResult(result: Result<T>): Resource<T> {
            return result.fold(
                onSuccess = { Success(it) },
                onFailure = { Error(it.message ?: "Unknown error", exception = it) }
            )
        }
    }
}
