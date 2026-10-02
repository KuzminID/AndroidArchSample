package ru.marwinka.androidarchsample.core.common

/** Result of a one-shot operation. */
sealed interface AppResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

/** Typed operation error. */
sealed interface AppError {
    val cause: Throwable?

    /** Network is unavailable or the server is unreachable. */
    data class Network(
        override val cause: Throwable? = null,
    ) : AppError

    /** Resource not found. */
    data class NotFound(
        override val cause: Throwable? = null,
    ) : AppError

    data class Unknown(
        override val cause: Throwable? = null,
    ) : AppError
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(value)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}
